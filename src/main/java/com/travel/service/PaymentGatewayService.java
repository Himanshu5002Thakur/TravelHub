package com.travel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class PaymentGatewayService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${app.payment.razorpay.key-id:}")
    private String razorpayKeyId;

    @Value("${app.payment.razorpay.key-secret:}")
    private String razorpayKeySecret;

    @Value("${app.payment.currency:INR}")
    private String currency;

    public PaymentGatewayService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public boolean isRazorpayEnabled() {
        return isPresent(razorpayKeyId) && isPresent(razorpayKeySecret);
    }

    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }

    public String getCurrency() {
        return currency;
    }

    public RazorpayOrder createOrder(String receipt, long amountPaise) {
        if (!isRazorpayEnabled()) {
            throw new IllegalStateException("Razorpay keys are not configured.");
        }

        try {
            String auth = Base64.getEncoder().encodeToString((razorpayKeyId + ":" + razorpayKeySecret).getBytes(StandardCharsets.UTF_8));

            String payload = objectMapper.createObjectNode()
                    .put("amount", amountPaise)
                    .put("currency", currency)
                    .put("receipt", receipt)
                    .put("payment_capture", 1)
                    .toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.razorpay.com/v1/orders"))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Razorpay order API failed: " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            String orderId = root.path("id").asText();
            long amount = root.path("amount").asLong();
            String orderCurrency = root.path("currency").asText(currency);
            return new RazorpayOrder(orderId, amount, orderCurrency);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create Razorpay order", ex);
        }
    }

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        if (!isRazorpayEnabled()) {
            return false;
        }

        try {
            String payload = orderId + "|" + paymentId;
            String expected = hmacSha256Hex(payload, razorpayKeySecret);
            return slowEquals(expected, signature);
        } catch (Exception ex) {
            return false;
        }
    }

    private static String hmacSha256Hex(String data, String secret) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            String h = Integer.toHexString(0xff & b);
            if (h.length() == 1) {
                hex.append('0');
            }
            hex.append(h);
        }
        return hex.toString();
    }

    private static boolean slowEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        int diff = a.length() ^ b.length();
        for (int i = 0; i < Math.min(a.length(), b.length()); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }

    private static boolean isPresent(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public record RazorpayOrder(String orderId, long amount, String currency) {}
}
