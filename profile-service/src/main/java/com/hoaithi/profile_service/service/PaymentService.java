package com.hoaithi.profile_service.service;

import com.hoaithi.profile_service.entity.Membership;
import com.hoaithi.profile_service.entity.MembershipTier;
import com.hoaithi.profile_service.entity.Payment;
import com.hoaithi.profile_service.entity.Profile;
import com.hoaithi.profile_service.enums.PaymentStatus;
import com.hoaithi.profile_service.exception.AppException;
import com.hoaithi.profile_service.exception.ErrorCode;
import com.hoaithi.profile_service.repository.MembershipRepository;
import com.hoaithi.profile_service.repository.MembershipTierRepository;
import com.hoaithi.profile_service.repository.PaymentRepository;
import com.hoaithi.profile_service.repository.ProfileRepository;
import com.hoaithi.profile_service.utils.ProfileUtil;
import com.hoaithidev.vidsonet_backend.dto.payment.CreatePaymentResponse;
import com.hoaithidev.vidsonet_backend.dto.payment.PaymentCaptureResponse;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PayPalHttpClient payPalClient;
    private final MembershipTierRepository membershipTierRepository;
    private final ProfileRepository profileRepository;
    private final MembershipRepository membershipRepository;
    private final PaymentRepository paymentRepository;
    private final ProfileUtil profileUtil;

    @Value("${paypal.success-url}")
    private String successUrl;

    @Value("${paypal.cancel-url}")
    private String cancelUrl;

    @Transactional
    public CreatePaymentResponse createPayment(Long membershipTierId) {
        String userId = profileUtil.getCurrentUserId();
        // 1. Lấy thông tin membership tier và user
        MembershipTier membershipTier = membershipTierRepository.findById(membershipTierId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_EXISTED));

        Profile user = profileRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_EXISTED));

        // Kiểm tra người dùng không tự đăng ký kênh của mình
        if (user.getId().equals(membershipTier.getUser().getId())) {
            throw new AppException(ErrorCode.USER_CHANNEL_SAME);
        }

        // Kiểm tra membership tier có active không
        if (!membershipTier.isActive()) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION); // Hoặc tạo thêm code ACTIVE_TIER_REQUIRED
        }

        // Kiểm tra xem user đã có membership active chưa
        Optional<Membership> existingMembership = membershipRepository.findActiveByUserIdAndChannelId(
                userId, membershipTier.getUser().getId());
        if (existingMembership.isPresent()) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION); // Hoặc tạo thêm code DUPLICATE_MEMBERSHIP
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
            purchaseUnitRequest.description("Membership: " + membershipTier.getName() + " for " + membershipTier.getUser().getFullName());
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
                    .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

            return CreatePaymentResponse.builder()
                    .paymentId(order.id())
                    .approvalUrl(approvalUrl)
                    .membershipId(membership.getId())
                    .build();

        } catch (IOException e) {
            log.error("Error creating PayPal payment: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        } catch (Exception e) {
            log.error("Unexpected error with PayPal payment: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Transactional
    public PaymentCaptureResponse capturePayment(String paymentId, String payerId) {
        String userId = profileUtil.getCurrentUserId();
        try {
            Payment payment = paymentRepository.findByTransactionId(paymentId)
                    .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_EXISTED));

            Membership membership = payment.getMembership();

            if (!membership.getUser().getId().equals(userId)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }

            OrdersCaptureRequest request = new OrdersCaptureRequest(paymentId);
            request.requestBody(new OrderRequest());

            HttpResponse<Order> response = payPalClient.execute(request);
            Order capturedOrder = response.result();

            if ("COMPLETED".equals(capturedOrder.status())) {
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setPaymentDate(LocalDateTime.now());

                String transactionId = null;
                if (capturedOrder.purchaseUnits() != null && !capturedOrder.purchaseUnits().isEmpty()) {
                    PurchaseUnit purchaseUnit = capturedOrder.purchaseUnits().getFirst();
                    if (purchaseUnit.payments() != null &&
                            purchaseUnit.payments().captures() != null &&
                            !purchaseUnit.payments().captures().isEmpty()) {

                        transactionId = purchaseUnit.payments().captures().getFirst().id();
                    }
                }

                if (transactionId == null) {
                    log.warn("Could not extract transaction ID from PayPal response");
                    transactionId = paymentId;
                }

                payment.setTransactionId(transactionId);
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
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }

        } catch (IOException e) {
            log.error("Error capturing PayPal payment: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        } catch (Exception e) {
            log.error("Unexpected error with PayPal capture: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Transactional
    public Membership confirmMembership(Long membershipId, String transactionId) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_EXISTED));

        Payment payment = membership.getPayment();

        if (!payment.getTransactionId().equals(transactionId)) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        if (!membership.isActive()) {
            membership.setActive(true);
            membership.setUpdatedAt(LocalDateTime.now());
            membershipRepository.save(membership);
        }

        return membership;
    }

    @Transactional
    public void cancelPayment(String paymentId) {
        Payment payment = paymentRepository.findByTransactionId(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_EXISTED));

        payment.setPaymentStatus(PaymentStatus.CANCELED);
        paymentRepository.save(payment);

        Membership membership = payment.getMembership();
        membership.setActive(false);
        membership.setUpdatedAt(LocalDateTime.now());
        membershipRepository.save(membership);
    }
}
