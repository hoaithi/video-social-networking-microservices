package com.hoaithidev.vidsonet_backend.service.impl;

import com.hoaithidev.vidsonet_backend.dto.payment.CreatePaymentResponse;
import com.hoaithidev.vidsonet_backend.dto.payment.PaymentCaptureResponse;
import com.hoaithidev.vidsonet_backend.enums.PaymentStatus;
import com.hoaithidev.vidsonet_backend.exception.ErrorCode;
import com.hoaithidev.vidsonet_backend.exception.ResourceNotFoundException;
import com.hoaithidev.vidsonet_backend.exception.VidsonetException;
import com.hoaithidev.vidsonet_backend.model.Membership;
import com.hoaithidev.vidsonet_backend.model.MembershipTier;
import com.hoaithidev.vidsonet_backend.model.Payment;
import com.hoaithidev.vidsonet_backend.model.User;
import com.hoaithidev.vidsonet_backend.repository.MembershipRepository;
import com.hoaithidev.vidsonet_backend.repository.MembershipTierRepository;
import com.hoaithidev.vidsonet_backend.repository.PaymentRepository;
import com.hoaithidev.vidsonet_backend.repository.UserRepository;
import com.hoaithidev.vidsonet_backend.service.PaymentService;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PayPalHttpClient payPalClient;
    private final MembershipTierRepository membershipTierRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final PaymentRepository paymentRepository;

    @Value("${paypal.success-url}")
    private String successUrl;

    @Value("${paypal.cancel-url}")
    private String cancelUrl;

    @Override
    @Transactional
    public CreatePaymentResponse createPayment(Long membershipTierId, Long userId) {
        // 1. Lấy thông tin membership tier và user
        MembershipTier membershipTier = membershipTierRepository.findById(membershipTierId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership tier not found with id: " + membershipTierId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Kiểm tra người dùng không tự đăng ký kênh của mình
        if (user.getId().equals(membershipTier.getUser().getId())) {
            throw new VidsonetException(ErrorCode.ALREADY_MEMBER, "You cannot subscribe to your own channel");
        }

        // Kiểm tra membership tier có active không
        if (!membershipTier.isActive()) {
            throw new VidsonetException(ErrorCode.IS_NOT_ACTIVE, "This membership tier is no longer active");
        }

        // Kiểm tra xem user đã có membership active chưa
        Optional<Membership> existingMembership = membershipRepository.findActiveByUserIdAndChannelId(
                userId, membershipTier.getUser().getId());
        if (existingMembership.isPresent()) {
            throw new VidsonetException(ErrorCode.DUPLICATE_RESOURCE, "You already have an active membership for this channel");
        }

        try {
            // 2. Tạo request cho PayPal
            OrdersCreateRequest request = new OrdersCreateRequest();
            request.prefer("return=representation");

            // 3. Cấu hình OrderRequest
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.checkoutPaymentIntent("CAPTURE");

            // Cấu hình application context (return URLs)
            ApplicationContext applicationContext = new ApplicationContext();
            applicationContext.returnUrl(successUrl + "?tier_id=" + membershipTierId);
            applicationContext.cancelUrl(cancelUrl);
            orderRequest.applicationContext(applicationContext);

            // Cấu hình purchase unit
            List<PurchaseUnitRequest> purchaseUnitRequests = new ArrayList<>();
            PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest();

            // Cấu hình amount
            AmountWithBreakdown amount = new AmountWithBreakdown();
            amount.currencyCode("USD");
            amount.value(membershipTier.getPrice().toString());

            purchaseUnitRequest.amountWithBreakdown(amount);
            purchaseUnitRequest.description("Membership: " + membershipTier.getName() + " for " + membershipTier.getUser().getChannelName());
            purchaseUnitRequests.add(purchaseUnitRequest);

            orderRequest.purchaseUnits(purchaseUnitRequests);

            request.requestBody(orderRequest);

            // 4. Gọi PayPal API để tạo order
            HttpResponse<Order> response = payPalClient.execute(request);
            Order order = response.result();

            // 5. Tạo membership và payment cho user
            Payment payment = Payment.builder()
                    .paymentDate(LocalDateTime.now())
                    .amount(membershipTier.getPrice())
                    .paymentMethod("PAYPAL")
                    .transactionId(order.id())
                    .paymentStatus(PaymentStatus.PENDING)
                    .build();

            Membership membership = Membership.builder()
                    .user(user)
                    .membershipTier(membershipTier)
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now().plusMonths(membershipTier.getDurationMonths()))
                    .isActive(false) // Sẽ được kích hoạt sau khi thanh toán thành công
                    .createdAt(LocalDateTime.now())
                    .payment(payment)
                    .build();

            payment.setMembership(membership);

            membershipRepository.save(membership);

            // 6. Lấy approval URL từ response
            String approvalUrl = order.links().stream()
                    .filter(link -> "approve".equals(link.rel()))
                    .findFirst()
                    .map(LinkDescription::href)
                    .orElseThrow(() -> new VidsonetException(ErrorCode.PAYMENT_FAILED, "Could not find approval URL"));

            return CreatePaymentResponse.builder()
                    .paymentId(order.id())
                    .approvalUrl(approvalUrl)
                    .membershipId(membership.getId())
                    .build();

        } catch (IOException e) {
            log.error("Error creating PayPal payment: {}", e.getMessage());
            throw new VidsonetException(ErrorCode.PAYMENT_FAILED, "Could not create PayPal payment: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error with PayPal payment: {}", e.getMessage());
            throw new VidsonetException(ErrorCode.PAYMENT_FAILED, "Error processing payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentCaptureResponse capturePayment(String paymentId, String payerId, Long userId) {
        try {
            // 1. Tìm membership và payment dựa trên paymentId
            Payment payment = paymentRepository.findByTransactionId(paymentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

            Membership membership = payment.getMembership();

            // 2. Kiểm tra xem payment có thuộc về user không
            if (!membership.getUser().getId().equals(userId)) {
                throw new VidsonetException(ErrorCode.FORBIDDEN, "Payment does not belong to this user");
            }

            // 3. Kiểm tra trạng thái payment
            if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
                throw new VidsonetException(ErrorCode.PAYMENT_FAILED, "Payment is not in PENDING state");
            }

            // 4. Tạo request capture payment
            OrdersCaptureRequest request = new OrdersCaptureRequest(paymentId);
            request.requestBody(new OrderRequest());

            // 5. Gọi PayPal API để capture payment
            HttpResponse<Order> response = payPalClient.execute(request);
            Order capturedOrder = response.result();

            // 6. Cập nhật trạng thái payment và membership
            if ("COMPLETED".equals(capturedOrder.status())) {
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setPaymentDate(LocalDateTime.now());

                // Lấy transaction ID từ capture
                String transactionId = null;
                if (capturedOrder.purchaseUnits() != null && !capturedOrder.purchaseUnits().isEmpty()) {
                    PurchaseUnit purchaseUnit = capturedOrder.purchaseUnits().get(0);
                    if (purchaseUnit.payments() != null &&
                            purchaseUnit.payments().captures() != null &&
                            !purchaseUnit.payments().captures().isEmpty()) {

                        transactionId = purchaseUnit.payments().captures().get(0).id();
                    }
                }

                if (transactionId == null) {
                    log.warn("Could not extract transaction ID from PayPal response");
                    transactionId = paymentId; // Fallback to payment ID if capture ID not found
                }

                payment.setTransactionId(transactionId);

                // Kích hoạt membership
                membership.setActive(true);
                membership.setStartDate(LocalDateTime.now());
                membership.setUpdatedAt(LocalDateTime.now());

                paymentRepository.save(payment);
                membershipRepository.save(membership);

                return PaymentCaptureResponse.builder()
                        .success(true)
                        .paymentId(paymentId)
                        .transactionId(transactionId)
                        .membershipId(membership.getId())
                        .message("Payment completed successfully")
                        .build();
            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                throw new VidsonetException(ErrorCode.PAYMENT_FAILED, "Payment capture failed: " + capturedOrder.status());
            }

        } catch (IOException e) {
            log.error("Error capturing PayPal payment: {}", e.getMessage());
            throw new VidsonetException(ErrorCode.PAYMENT_FAILED, "Could not capture PayPal payment: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error with PayPal capture: {}", e.getMessage());
            throw new VidsonetException(ErrorCode.PAYMENT_FAILED, "Error capturing payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Membership confirmMembership(Long membershipId, String transactionId) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with id: " + membershipId));

        Payment payment = membership.getPayment();

        // Kiểm tra transaction ID
        if (!payment.getTransactionId().equals(transactionId)) {
            throw new VidsonetException(ErrorCode.INVALID_REQUEST, "Transaction ID does not match");
        }

        // Kiểm tra trạng thái payment
        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new VidsonetException(ErrorCode.PAYMENT_FAILED, "Payment is not completed");
        }

        // Kích hoạt membership nếu chưa active
        if (!membership.isActive()) {
            membership.setActive(true);
            membership.setUpdatedAt(LocalDateTime.now());
            membershipRepository.save(membership);
        }

        return membership;
    }

    @Override
    @Transactional
    public void cancelPayment(String paymentId) {
        Payment payment = paymentRepository.findByTransactionId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        // Cập nhật trạng thái payment
        payment.setPaymentStatus(PaymentStatus.CANCELED);
        paymentRepository.save(payment);

        // Cập nhật membership
        Membership membership = payment.getMembership();
        membership.setActive(false);
        membership.setUpdatedAt(LocalDateTime.now());
        membershipRepository.save(membership);
    }
}