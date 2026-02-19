# WebSocket Implementation for Transaction Notifications

This document describes the WebSocket implementation for real-time transaction notifications in the Vend application.

## Overview

The WebSocket functionality allows the application to send real-time notifications to clients when transactions are processed. This enables features like:

- Real-time transaction dashboards
- Instant notifications for staff and administrators
- Live monitoring of device activities
- Immediate feedback for payment processing

## Architecture

The implementation consists of the following components:

1. **WebSocketConfig**: Configuration class that sets up the WebSocket endpoints and message broker
2. **WebSocketMessage**: Model class for the messages sent through WebSocket
3. **WebSocketService**: Service class that handles sending messages to different topics

## Topics

Messages are sent to the following topics:

- `/topic/transactions`: All transactions
- `/topic/transactions/company/{companyId}`: Transactions for a specific company
- `/topic/transactions/staff/{staffId}`: Transactions for a specific staff member
- `/topic/transactions/device/{deviceId}`: Transactions for a specific device

## Message Format

Each WebSocket message contains the following information:

```json
{
  "type": "TRANSACTION_PROCESSED",
  "transactionId": "TRX123456",
  "referenceNumber": "REF123456",
  "paystackReference": "PST123456",
  "amount": 100.00,
  "status": "COMPLETED",
  "companyId": 1,
  "companyName": "Example Company",
  "deviceId": 2,
  "deviceName": "Vending Machine 1",
  "staffId": 3,
  "staffName": "John Doe",
  "timestamp": "2023-05-01T12:34:56.789Z"
}
```

## How to Use

### Backend

The WebSocket service is automatically injected into the `TransactionServiceImpl` class and sends a notification after a transaction is processed. No additional configuration is needed on the backend.

### Connection Details

The WebSocket server is accessible at the following endpoints:

- **Production Environment**: 
  - Host: `eazybrewserver.com`
  - Port: `9090`
  - WebSocket URL: `ws://eazybrewserver.com:9090/ws`
  - SockJS URL: `http://eazybrewserver.com:9090/ws`

- **Development Environment**:
  - Host: `dev.eazybrewserver.com`
  - Port: `9090`
  - WebSocket URL: `ws://dev.eazybrewserver.com:9090/ws`
  - SockJS URL: `http://dev.eazybrewserver.com:9090/ws`

- **Local Development**:
  - Host: `localhost`
  - Port: `9090`
  - WebSocket URL: `ws://localhost:9090/ws`
  - SockJS URL: `http://localhost:9090/ws`

### Device Identification and Notifications

When working with WebSocket notifications, it's important to understand how devices are identified:

1. **Device Serial Number vs. Database ID**:
   - Each device has a unique serial number (`deviceId` in the code) that identifies it in the system
   - Each device also has a database ID (primary key) that is used in WebSocket topic subscriptions
   - The WebSocket topics use the database ID, not the device serial number

2. **Getting Your Device's Database ID**:
   - When a device is registered in the system, it receives a database ID
   - To get your device's database ID, make a request to:
     ```
     GET /api/transactions/device/{deviceId}
     ```
     where `{deviceId}` is your device's serial number
   - The response will include both the `deviceId` (serial number) and `id` (database ID)

3. **Subscribing to Device-Specific Notifications**:
   - Use the database ID (not the serial number) when subscribing to device-specific topics
   - The correct format is: `/topic/transactions/device/{databaseId}`

### Android Client Connection

For Android clients, you can use libraries like OkHttp with WebSockets or StompProtocolAndroid to connect to the WebSocket server:

```kotlin
// Example using OkHttp WebSocket for Development Environment
val client = OkHttpClient()
val request = Request.Builder()
    .url("ws://dev.eazybrewserver.com:9090/ws")
    .build()
val webSocket = client.newWebSocket(request, webSocketListener)

// Example using StompProtocolAndroid for Development Environment
val url = "ws://dev.eazybrewserver.com:9090/ws"
val client = OkHttpClient()
val stomp = StompClient(url, client)
stomp.connect()

// First, get your device's database ID
// Example: Make an HTTP request to /api/transactions/device/{your-device-serial-number}
// and extract the 'id' field from the response

// Then subscribe to device-specific topics using the database ID
val databaseId = 123 // Replace with your device's actual database ID
stomp.topic("/topic/transactions/device/$databaseId").subscribe { message ->
    // Handle message
}
```

### Frontend

To connect to the WebSocket from a client application:

1. Connect to the WebSocket endpoint:

```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);
stompClient.connect({}, onConnected, onError);
```

2. Subscribe to the desired topics:

```javascript
function onConnected() {
  // Subscribe to all transactions
  stompClient.subscribe('/topic/transactions', onMessageReceived);

  // Subscribe to company-specific transactions
  stompClient.subscribe(`/topic/transactions/company/${companyId}`, onMessageReceived);

  // Subscribe to staff-specific transactions
  stompClient.subscribe(`/topic/transactions/staff/${staffId}`, onMessageReceived);

  // Subscribe to device-specific transactions
  stompClient.subscribe(`/topic/transactions/device/${deviceId}`, onMessageReceived);
}
```

3. Handle received messages:

```javascript
function onMessageReceived(message) {
  const transaction = JSON.parse(message.body);
  // Process the transaction notification
  console.log('New transaction:', transaction);
}
```

## Demo

A demo HTML page is available at `/websocket-demo.html` that demonstrates how to connect to the WebSocket and receive messages. This page can be used for testing and as a reference for frontend developers.

## Testing

The WebSocket implementation includes unit tests in `WebSocketServiceTest.java` that verify:

1. Messages are sent to all appropriate topics
2. Staff-specific messages are only sent when a staff member is associated with the transaction
3. Exceptions in the messaging service are handled gracefully

## Dependencies

The WebSocket functionality requires the following dependency in the `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```
