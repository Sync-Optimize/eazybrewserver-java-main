package com.eazybrew.vend.websocket;

import com.eazybrew.vend.dto.response.TransactionResponse;
import com.eazybrew.vend.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for sending WebSocket messages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send a transaction notification to all clients subscribed to the company's
     * topic
     * 
     * @param transaction the transaction
     */
    public void sendTransactionNotification(TransactionResponse transaction) {
        try {
            WebSocketMessage message = WebSocketMessage.fromTransaction(transaction, "TRANSACTION_PROCESSED");

            // Also send to device-specific topic
            String deviceDestination = "/topic/transactions/device/" + transaction.getDeviceId();
            messagingTemplate.convertAndSend(deviceDestination, message);
            log.info("Sent transaction notification to {}: {}", deviceDestination, message);

            // // Send to company-specific topic
            // String destination = "/topic/transactions/company/" +
            // transaction.getCompany().getId();
            // messagingTemplate.convertAndSend(destination, message);
            // log.info("Sent transaction notification to {}: {}", destination, message);
            //
            // // If staff is involved, also send to staff-specific topic
            // if (transaction.getStaff() != null) {
            // String staffDestination = "/topic/transactions/staff/" +
            // transaction.getStaff().getId();
            // messagingTemplate.convertAndSend(staffDestination, message);
            // log.info("Sent transaction notification to {}: {}", staffDestination,
            // message);
            // }

            // // Send to general transactions topic
            // messagingTemplate.convertAndSend("/topic/transactions", message);
            // log.info("Sent transaction notification to /topic/transactions: {}",
            // message);
        } catch (Exception e) {
            // Log error but don't throw exception to avoid disrupting the main transaction
            // flow
            log.error("Error sending WebSocket message: {}", e.getMessage(), e);
        }
    }
}