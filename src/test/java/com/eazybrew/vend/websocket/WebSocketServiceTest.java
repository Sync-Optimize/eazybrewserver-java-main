//package com.eazybrew.vend.websocket;
//
//import com.eazybrew.vend.model.*;
//import com.eazybrew.vend.model.enums.DeviceType;
//import com.eazybrew.vend.model.enums.PaymentMethod;
//import com.eazybrew.vend.model.enums.TransactionStatus;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//class WebSocketServiceTest {
//
//    @Mock
//    private SimpMessagingTemplate messagingTemplate;
//
//    @Captor
//    private ArgumentCaptor<WebSocketMessage> messageCaptor;
//
//    private WebSocketService webSocketService;
//    private Transaction transaction;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        webSocketService = new WebSocketService(messagingTemplate);
//
//        // Create test data
//        Company company = new Company();
//        company.setId(1L);
//        company.setName("Test Company");
//
//        Device device = new Device();
//        device.setId(2L);
//        device.setDeviceName("Test Device");
//        device.setDeviceType(DeviceType.VENDING_MACHINE);
//        device.setCompany(company);
//
//        Staff staff = new Staff();
//        staff.setId(3L);
//        staff.setFullName("Test Staff");
//        staff.setEmail("staff@example.com");
//        staff.setCompany(company);
//
//        transaction = new Transaction();
//        transaction.setId(4L);
//        transaction.setTransactionId("TRX123456");
//        transaction.setReferenceNumber("REF123456");
//        transaction.setPaystackReference("PST123456");
//        transaction.setAmount(new BigDecimal("100.00"));
//        transaction.setStatus(TransactionStatus.COMPLETED);
//        transaction.setCompany(company);
//        transaction.setDevice(device);
//        transaction.setStaff(staff);
//        transaction.setPaymentMethod(PaymentMethod.CREDIT_CARD);
//        transaction.setDateCreated(LocalDateTime.now());
//    }
//
//    @Test
//    void sendTransactionNotification_shouldSendToAllTopics() {
//        // When
//        webSocketService.sendTransactionNotification(transaction);
//
//        // Then
//        // Verify message sent to company topic
//        verify(messagingTemplate).convertAndSend(
//                eq("/topic/transactions/company/1"),
//                messageCaptor.capture());
//
//        // Verify message sent to staff topic
//        verify(messagingTemplate).convertAndSend(
//                eq("/topic/transactions/staff/3"),
//                messageCaptor.capture());
//
//        // Verify message sent to device topic
//        verify(messagingTemplate).convertAndSend(
//                eq("/topic/transactions/device/2"),
//                messageCaptor.capture());
//
//        // Verify message sent to general topic
//        verify(messagingTemplate).convertAndSend(
//                eq("/topic/transactions"),
//                messageCaptor.capture());
//
//        // Verify message content (using the last captured message)
//        WebSocketMessage message = messageCaptor.getValue();
//        assertEquals("TRANSACTION_PROCESSED", message.getType());
//        assertEquals("TRX123456", message.getTransactionId());
//        assertEquals("REF123456", message.getReferenceNumber());
//        assertEquals("PST123456", message.getPaystackReference());
//        assertEquals(new BigDecimal("100.00"), message.getAmount());
//        assertEquals("COMPLETED", message.getStatus());
//        assertEquals(1L, message.getCompanyId());
//        assertEquals("Test Company", message.getCompanyName());
//        assertEquals(2L, message.getDeviceId());
//        assertEquals("Test Device", message.getDeviceName());
//        assertEquals(3L, message.getStaffId());
//        assertEquals("Test Staff", message.getStaffName());
//        assertNotNull(message.getTimestamp());
//    }
//
//    @Test
//    void sendTransactionNotification_withoutStaff_shouldNotSendToStaffTopic() {
//        // Given
//        transaction.setStaff(null);
//
//        // When
//        webSocketService.sendTransactionNotification(transaction);
//
//        // Then
//        // Verify message sent to company topic
//        verify(messagingTemplate).convertAndSend(
//                eq("/topic/transactions/company/1"),
//                any(WebSocketMessage.class));
//
//        // Verify message NOT sent to staff topic
//        verify(messagingTemplate, never()).convertAndSend(
//                eq("/topic/transactions/staff/3"),
//                any(WebSocketMessage.class));
//
//        // Verify message sent to device topic
//        verify(messagingTemplate).convertAndSend(
//                eq("/topic/transactions/device/2"),
//                any(WebSocketMessage.class));
//
//        // Verify message sent to general topic
//        verify(messagingTemplate).convertAndSend(
//                eq("/topic/transactions"),
//                any(WebSocketMessage.class));
//    }
//
//    @Test
//    void sendTransactionNotification_withException_shouldNotPropagateException() {
//        // Given
//        doThrow(new RuntimeException("Test exception"))
//                .when(messagingTemplate).convertAndSend(anyString(), any(WebSocketMessage.class));
//
//        // When & Then
//        assertDoesNotThrow(() -> webSocketService.sendTransactionNotification(transaction));
//    }
//}
