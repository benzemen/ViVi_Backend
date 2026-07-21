<div align="center">
  <h1>💬 ViVi Chat Backend</h1>
  <p><strong>The robust, real-time Spring Boot backend powering the ViVi Chat application.</strong></p>

  <!-- Placeholder for Live Link -->
  <p>
    <a href="YOUR_LIVE_LINK_HERE"><strong>View Live Demo</strong></a> 
    ·
    <a href="#features">Features</a>
    ·
    <a href="#api-endpoints">API</a>
  </p>
</div>

---

## 📖 Overview

ViVi Chat Backend is a scalable, modern real-time messaging server built with **Java** and **Spring Boot**. It handles real-time WebSocket communication, secure authentication flows, persistent storage using MongoDB, and intelligent AI features powered by Google Gemini.

## ✨ Key Features

- **Real-Time Communication**: Utilizes WebSockets (STOMP over SockJS) for instantaneous message delivery.
- **Secure Authentication**: Stateless JWT-based authentication paired with Google OAuth2 login via Spring Security.
- **MongoDB Persistence**: Highly optimized database schema storing messages independently for infinite database-level pagination, bypassing MongoDB document size limits.
- **AI-Powered Catch Up**: Integrates with the Google Gemini API to generate concise "catch-me-up" summaries of long chat room histories.
- **Content Moderation**: Built-in AI moderation service to automatically flag, filter, and prevent inappropriate content.
- **Message Management**: Users can delete messages globally or utilize the "Delete for me" feature to hide specific messages from their personal view only.
- **CORS Configured**: Fully configured to handle Cross-Origin Resource Sharing for seamless frontend integration.

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3
- **Language**: Java 17+
- **Database**: MongoDB (Spring Data MongoDB)
- **Security**: Spring Security, OAuth2, JWT (io.jsonwebtoken)
- **Real-time**: Spring WebSocket / STOMP
- **AI Integration**: Google Cloud AI (Gemini)
- **Build Tool**: Maven

## 🚀 Getting Started

### Prerequisites
- JDK 17 or higher
- MongoDB Atlas cluster or local MongoDB instance
- Google Cloud Console project (for OAuth2 credentials and Gemini API Key)

### Configuration

Create an `application.properties` or `application.yml` file in `src/main/resources` and configure the following environment variables:

```properties
# MongoDB Connection
spring.data.mongodb.uri=mongodb+srv://<username>:<password>@cluster0...

# OAuth2 Google Credentials
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET

# JWT Secret Key
app.jwt.secret=YOUR_LONG_SECURE_JWT_SECRET
app.jwt.expiration-ms=86400000

# Google Gemini API Key for Summarization and Moderation
app.gemini.api-key=YOUR_GEMINI_API_KEY
```

### Running Locally

```bash
# Clean and package the application
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```
The server will start on `http://localhost:8080`.

## 🌐 API Endpoints

### Authentication
- `GET /oauth2/authorization/google` - Initiates Google OAuth2 login flow.
- `GET /api/v1/auth/me` - Validates JWT and returns current user details.

### Rooms
- `POST /api/v1/rooms` - Create a new chat room.
- `GET /api/v1/rooms` - List all available rooms.
- `GET /api/v1/rooms/{roomId}` - Get room details.
- `DELETE /api/v1/rooms/{roomId}` - Delete a room and its messages.

### Messages
- `GET /api/v1/rooms/{roomId}/messages?page=0&size=20` - Retrieve paginated messages.
- `GET /api/v1/rooms/{roomId}/summary` - Generate an AI summary of recent messages.
- `DELETE /api/v1/rooms/{roomId}/messages/{messageId}` - Delete a message for everyone.
- `POST /api/v1/rooms/{roomId}/messages/{messageId}/hide` - Hide a message for the current user ("Delete for me").

### WebSocket
- **Connect**: `ws://localhost:8080/chat`
- **Subscribe**: `/topic/room/{roomId}`
- **Publish**: `/app/sendMessage/{roomId}`
