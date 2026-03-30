package com.project.artisanmarketplace.service;

import com.project.artisanmarketplace.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOrderConfirmation(String toEmail, String name, Order order) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("Order Confirmed — Artisan Handcraft #" + order.getId());
        msg.setText(
            "Hello " + name + ",\n\n" +
            "Your order #" + order.getId() + " has been confirmed!\n\n" +
            "Total: ₹" + String.format("%.2f", order.getTotalAmount()) + "\n" +
            "Status: " + order.getStatus() + "\n\n" +
            "Shipping to: " + order.getShippingAddress() + "\n\n" +
            "Thank you for supporting Indian artisans!\n\n" +
            "— Artisan Handcraft Team"
        );
        mailSender.send(msg);
    }

    public void sendShippingUpdate(String toEmail, String name, Long orderId, String trackingId) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("Your order #" + orderId + " has been shipped!");
        msg.setText(
            "Hello " + name + ",\n\n" +
            "Great news! Your order #" + orderId + " is on its way.\n" +
            "Tracking ID: " + trackingId + "\n\n" +
            "— Artisan Handcraft Team"
        );
        mailSender.send(msg);
    }
}
