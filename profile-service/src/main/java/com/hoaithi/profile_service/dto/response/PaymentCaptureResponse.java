package com.hoaithidev.vidsonet_backend.dto.payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCaptureResponse {
    private boolean success;
    private String paymentId;
    private String transactionId;
    private Long membershipId;
    private String message;
}