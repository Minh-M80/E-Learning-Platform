package com.example.E_Learning_Platform.service;

import com.example.E_Learning_Platform.configuration.VnPayProperties;
import com.example.E_Learning_Platform.dto.response.PaymentCallbackResponse;
import com.example.E_Learning_Platform.entity.Order;
import com.example.E_Learning_Platform.entity.User;
import com.example.E_Learning_Platform.enums.OrderStatus;
import com.example.E_Learning_Platform.enums.Role;
import com.example.E_Learning_Platform.exception.AppException;
import com.example.E_Learning_Platform.exception.ErrorCode;
import com.example.E_Learning_Platform.repository.OrderRepository;
import com.example.E_Learning_Platform.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VnPayService {

    private static final DateTimeFormatter VNPAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnPayProperties vnPayProperties;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;

    public String createPaymentUrl(String orderId) {
        validateConfiguration();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOTOUND));

        validateCanCreatePaymentUrl(order);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.ORDER_INVALID_STATUS);
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime expiredAt = now.plusMinutes(15);

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnPayProperties.getVersion());
        vnpParams.put("vnp_Command", vnPayProperties.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        vnpParams.put("vnp_Amount", toVnPayAmount(order.getTotalPrice()));
        vnpParams.put("vnp_CurrCode", vnPayProperties.getCurrCode());
        vnpParams.put("vnp_TxnRef", order.getId());
        vnpParams.put("vnp_OrderInfo", order.getVnpOrderInfo());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", vnPayProperties.getLocale());
        vnpParams.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
        vnpParams.put("vnp_IpAddr", resolveClientIp());
        vnpParams.put("vnp_CreateDate", now.format(VNPAY_DATE_FORMAT));
        vnpParams.put("vnp_ExpireDate", expiredAt.format(VNPAY_DATE_FORMAT));

        String query = buildQueryString(vnpParams);
        String secureHash = hmacSHA512(vnPayProperties.getHashSecret(), buildHashData(vnpParams));

        return vnPayProperties.getPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    public PaymentCallbackResponse handleVnPayReturn(Map<String, String> params) {
        String orderId = params.get("vnp_TxnRef");
        String transactionNo = params.get("vnp_TransactionNo");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");

        if (!verifyPaymentResponse(params)) {
            return PaymentCallbackResponse.builder()
                    .success(false)
                    .orderId(orderId)
                    .transactionNo(transactionNo)
                    .responseCode(responseCode)
                    .transactionStatus(transactionStatus)
                    .message("VNPay response is not valid")
                    .build();
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOTOUND));

        boolean paymentSuccess = "00".equals(responseCode) && "00".equals(transactionStatus);
        if (paymentSuccess) {
            orderService.markOrderCompleted(orderId, transactionNo);
            enrollmentService.enrollUserFromOrder(orderId);
        } else {
            orderService.markOrderFailed(orderId);
        }

        Order refreshedOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOTOUND));

        return PaymentCallbackResponse.builder()
                .success(paymentSuccess)
                .orderId(orderId)
                .orderStatus(refreshedOrder.getStatus().name())
                .transactionNo(transactionNo)
                .responseCode(responseCode)
                .transactionStatus(transactionStatus)
                .message(paymentSuccess ? "Payment successful" : "Payment failed")
                .build();
    }

    public boolean verifyPaymentResponse(Map<String, String> params) {
        validateConfiguration();

        String secureHash = params.get("vnp_SecureHash");
        String orderId = params.get("vnp_TxnRef");
        String amount = params.get("vnp_Amount");

        if (secureHash == null || orderId == null || amount == null) {
            return false;
        }

        Map<String, String> hashFields = new HashMap<>();
        params.forEach((key, value) -> {
            if (value == null || value.isBlank()) {
                return;
            }
            if ("vnp_SecureHash".equals(key) || "vnp_SecureHashType".equals(key)) {
                return;
            }
            hashFields.put(key, value);
        });

        String expectedHash = hmacSHA512(vnPayProperties.getHashSecret(), buildHashData(hashFields));
        if (!expectedHash.equalsIgnoreCase(secureHash)) {
            return false;
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }

        if (!toVnPayAmount(order.getTotalPrice()).equals(amount)) {
            return false;
        }

        String transactionNo = params.get("vnp_TransactionNo");
        if (transactionNo != null && !transactionNo.isBlank()) {
            Order existingOrder = orderRepository.findByVnpTransactionNo(transactionNo).orElse(null);
            if (existingOrder != null && !existingOrder.getId().equals(orderId)) {
                return false;
            }
        }

        return true;
    }

    private void validateCanCreatePaymentUrl(Order order) {
        User currentUser = getCurrentUser();
        boolean isAdmin = hasRole(Role.ADMIN.name());
        boolean isStudentOwner = hasRole(Role.STUDENT.name())
                && order.getUser() != null
                && order.getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isStudentOwner) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateConfiguration() {
        if (isBlank(vnPayProperties.getTmnCode())
                || isBlank(vnPayProperties.getHashSecret())
                || isBlank(vnPayProperties.getPayUrl())
                || isBlank(vnPayProperties.getReturnUrl())) {
            throw new AppException(ErrorCode.PAYMENT_CONFIG_INVALID);
        }
    }

    private String buildQueryString(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();
        boolean first = true;
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue == null || fieldValue.isBlank()) {
                continue;
            }

            if (!first) {
                query.append('&');
            }

            query.append(encode(fieldName))
                    .append('=')
                    .append(encode(fieldValue));
            first = false;
        }
        return query.toString();
    }

    private String buildHashData(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        boolean first = true;
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue == null || fieldValue.isBlank()) {
                continue;
            }

            if (!first) {
                hashData.append('&');
            }

            hashData.append(encode(fieldName))
                    .append('=')
                    .append(encode(fieldValue));
            first = false;
        }
        return hashData.toString();
    }

    private String resolveClientIp() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String forwardedFor = request.getHeader("X-FORWARDED-FOR");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                return forwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return "127.0.0.1";
    }

    private String toVnPayAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).toBigIntegerExact().toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKeySpec);
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder(bytes.length * 2);
            for (byte currentByte : bytes) {
                hash.append(String.format("%02x", currentByte & 0xff));
            }
            return hash.toString();
        } catch (Exception exception) {
            throw new AppException(ErrorCode.PAYMENT_CONFIG_INVALID);
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities()
                .stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
