package com.example.E_Learning_Platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCallbackResponse {
    private boolean success;
    private String orderId;
    private String orderStatus;
    private String transactionNo;
    private String responseCode;
    private String transactionStatus;
    private String message;
}
