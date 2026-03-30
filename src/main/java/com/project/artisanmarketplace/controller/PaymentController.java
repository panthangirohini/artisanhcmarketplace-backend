package com.project.artisanmarketplace.controller;

import com.project.artisanmarketplace.model.Order;
import com.project.artisanmarketplace.repository.OrderRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HexFormat;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Autowired
    private OrderRepository orderRepository;

    // Step 1 — Frontend calls this to create a Razorpay order
    // Returns razorpay_order_id which the frontend uses to open the payment popup
    @PostMapping("/create-order")
    public ResponseEntity<?> createRazorpayOrder(@RequestBody Map<String, Object> body) {
        try {
            int amountPaise = (int) (Double.parseDouble(body.get("amount").toString()) * 100);
            String currency = "INR";

            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            // Build Razorpay order creation payload safely and consistently
            JSONObject options = new JSONObject(Map.of(
                "amount", amountPaise,
                "currency", currency,
                "receipt", "order_" + System.currentTimeMillis(),
                "payment_capture", 1
            ));

            com.razorpay.Order razorpayOrder = client.orders.create(options);

            return ResponseEntity.ok(Map.of(
                "razorpayOrderId", razorpayOrder.get("id").toString(),
                "amount",          amountPaise,
                "currency",        currency,
                "keyId",           keyId
            ));
        } catch (RazorpayException e) {
            return ResponseEntity.internalServerError().body("Failed to create payment: " + e.getMessage());
        }
    }

    // Step 2 — Frontend calls this after successful payment to verify signature
    // Razorpay signs razorpay_order_id|razorpay_payment_id with the key secret
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> body) {
        try {
            String razorpayOrderId  = body.get("razorpayOrderId");
            String razorpayPaymentId = body.get("razorpayPaymentId");
            String razorpaySignature = body.get("razorpaySignature");
            Long   appOrderId        = Long.parseLong(body.get("appOrderId"));

            // Verify HMAC-SHA256 signature
            String message = razorpayOrderId + "|" + razorpayPaymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(), "HmacSHA256"));
            String expectedSignature = HexFormat.of().formatHex(mac.doFinal(message.getBytes()));

            if (!expectedSignature.equals(razorpaySignature)) {
                return ResponseEntity.badRequest().body("Payment verification failed — signature mismatch");
            }

            // Mark the order as CONFIRMED with the payment ID
            orderRepository.findById(appOrderId).ifPresent(order -> {
                order.setPaymentId(razorpayPaymentId);
                order.setStatus(Order.OrderStatus.CONFIRMED);
                orderRepository.save(order);
            });

            return ResponseEntity.ok(Map.of(
                "verified", true,
                "paymentId", razorpayPaymentId
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Verification error: " + e.getMessage());
        }
    }
}
