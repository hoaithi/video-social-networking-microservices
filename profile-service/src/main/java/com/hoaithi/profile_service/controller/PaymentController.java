package com.hoaithi.profile_service.controller;

import com.hoaithi.profile_service.dto.response.ApiResponse;
import com.hoaithi.profile_service.service.PaymentService;
import com.hoaithidev.vidsonet_backend.dto.payment.CreatePaymentResponse;
import com.hoaithidev.vidsonet_backend.dto.payment.PaymentCaptureRequest;
import com.hoaithidev.vidsonet_backend.dto.payment.PaymentCaptureResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CreatePaymentResponse>> createPayment(
            @RequestParam Long membershipTierId) {

        CreatePaymentResponse response = paymentService.createPayment(membershipTierId);

        return ResponseEntity.ok(ApiResponse.<CreatePaymentResponse>builder()
                .message("Payment created successfully")
                .result(response)
                .build());
    }

    @PostMapping("/capture")
    public ResponseEntity<ApiResponse<PaymentCaptureResponse>> capturePayment(
           @RequestBody PaymentCaptureRequest request) {
        log.info("Payment captured ....................");
        log.info("paymentId: {}", request.getPaymentId());
        log.info("payerID: {}", request.getPayerID());

        PaymentCaptureResponse response = paymentService.capturePayment(request.getPaymentId(), request.getPayerID());

        return ResponseEntity.ok(ApiResponse.<PaymentCaptureResponse>builder()
                .message("Payment captured successfully")
                .result(response)
                .build());
    }

    @GetMapping("/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> cancelPayment(
            @RequestParam String paymentId) {

        paymentService.cancelPayment(paymentId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Payment cancelled")
                .build());
    }
}