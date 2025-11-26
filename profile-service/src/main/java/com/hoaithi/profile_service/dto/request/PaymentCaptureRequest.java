package com.hoaithidev.vidsonet_backend.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCaptureRequest {
    private String paymentId;
    private String payerID;
}
