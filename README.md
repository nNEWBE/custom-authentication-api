# 🔐 Custom Authentication API

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen?style=for-the-badge&logo=springboot" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=openjdk" alt="Java"/>
  <img src="https://img.shields.io/badge/JWT-Authentication-blue?style=for-the-badge&logo=jsonwebtokens" alt="JWT"/>
  <img src="https://img.shields.io/badge/PostgreSQL-Database-316192?style=for-the-badge&logo=postgresql" alt="PostgreSQL"/>
</p>

A production-ready, secure **Spring Boot REST API** implementing JWT-based authentication with email verification. Built following clean architecture principles and security best practices.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🔑 **JWT Authentication** | Stateless, secure token-based authentication |
| 📧 **Email Verification** | Unique, time-limited verification links (10 min expiry) |
| 🔒 **Login Protection** | Users cannot login until email is verified |
| 🔄 **Auto-resend** | Verification email automatically resent on unverified login attempt |
| ⏱️ **Rate Limiting** | 5-minute cooldown between verification email requests |
| 🚫 **Token Invalidation** | Old verification links become invalid when new one is generated |
| ✅ **Input Validation** | Email format, password strength validation |
| 🗄️ **PostgreSQL Database** | Production-grade persistent storage |
| 📨 **Async Email Sending** | Non-blocking email dispatch using Spring Events |

---

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| **Spring Boot 4.0.2** | Application framework |
| **Spring Security 7** | Authentication & authorization |
| **Spring Data JPA** | Database ORM |
| **PostgreSQL** | Primary database |
| **JWT (jjwt 0.11.5)** | Token-based authentication |
| **JavaMailSender** | Email delivery |
| **Lombok** | Boilerplate reduction |
| **Gradle** | Build automation |

---

## 📁 Project Structure

```
src/main/java/com/example/custom_authentication/
├── 📂 config/
│   ├── SecurityConfig.java            # Spring Security & JWT configuration
│   └── JwtAuthenticationEntryPoint.java
├── 📂 controller/
│   ├── AuthController.java            # Authentication endpoints
│   └── TestController.java            # Protected test endpoint
├── 📂 dto/
│   ├── RegisterRequest.java           # Registration payload with validation
│   ├── LoginRequest.java              # Login payload
│   ├── AuthResponse.java              # Auth response with token
│   └── ApiResponse.java               # Standard API response wrapper
├── 📂 entity/
│   └── User.java                      # User entity with verification fields
├── 📂 event/
│   └── UserRegisteredEvent.java       # Event for async email sending
├── 📂 exception/
│   ├── EmailCooldownException.java    # Rate limiting exception
│   ├── UserNotVerifiedException.java  # Verification exception
│   └── GlobalExceptionHandler.java    # Centralized error handling
├── 📂 filter/
│   └── JwtAuthenticationFilter.java   # JWT validation filter
├── 📂 listener/
│   └── UserRegistrationListener.java  # Async email event handler
├── 📂 repository/
│   └── UserRepository.java            # User data access
├── 📂 service/
│   ├── UserService.java               # Core business logic
│   ├── EmailService.java              # Email sending with HTML templates
│   └── CustomUserDetailsService.java  # Spring Security integration
├── 📂 util/
│   └── JwtUtil.java                   # JWT generation & validation
└── CustomAuthenticationProjectApplication.java
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 25+**
- **PostgreSQL 15+**
- **Gradle 8+**

### 1️⃣ Database Setup

```sql
-- Create the database
CREATE DATABASE custom_auth_db;
```

### 2️⃣ Configuration

The application uses **Spring Profiles** for environment separation:

| Profile | Database | Email | Use Case |
|---------|----------|-------|----------|
| `dev` | PostgreSQL | Mailtrap (testing) | Development |
| `prod` | PostgreSQL | Gmail (real) | Production |

#### Configure Email (choose one):

**Option A: Mailtrap (Development)**
```yaml
# src/main/resources/application-dev.yaml
spring:
  mail:
    host: sandbox.smtp.mailtrap.io
    port: 2525
    username: your-mailtrap-username
    password: your-mailtrap-password
```

**Option B: Gmail (Production)**
```yaml
# src/main/resources/application-prod.yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password  # Generate at https://myaccount.google.com/apppasswords
```

### 3️⃣ Run the Application

```bash
# Development mode (Mailtrap)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Production mode (Gmail)
./gradlew bootRun --args='--spring.profiles.active=prod'
```

The API will be available at `http://localhost:8080`

---

## 📡 API Reference

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

<details>
<summary>📤 Response (201 Created)</summary>

```json
{
  "success": true,
  "message": "User registered successfully. Please check your email for verification.",
  "data": null
}
```
</details>

---

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

<details>
<summary>📤 Response (200 OK) - Verified User</summary>

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIi..."
  }
}
```
</details>

<details>
<summary>📤 Response (403 Forbidden) - Unverified User</summary>

```json
{
  "success": false,
  "message": "Account not verified. Verification email sent.",
  "data": null
}
```
</details>

<details>
<summary>📤 Response (429 Too Many Requests) - Cooldown Active</summary>

```json
{
  "success": false,
  "message": "Please wait 4 minute(s) before requesting a new verification link.",
  "data": null
}
```
</details>

---

#### Verify Email
```http
GET /api/auth/verify?token={verification-token}
```

<details>
<summary>📤 Response (200 OK)</summary>

```json
{
  "success": true,
  "message": "Account verified successfully. You can now login.",
  "data": null
}
```
</details>

---

#### Resend Verification Email
```http
POST /api/auth/resend-verification?email=user@example.com
```

<details>
<summary>📤 Response (200 OK)</summary>

```json
{
  "success": true,
  "message": "Verification email sent successfully.",
  "data": null
}
```
</details>

---

### Protected Endpoints

#### Test Endpoint (Requires Authentication)
```http
GET /api/test
Authorization: Bearer {jwt-token}
```

<details>
<summary>📤 Response (200 OK)</summary>

```json
{
  "success": true,
  "message": "You have accessed a protected endpoint successfully!",
  "data": {
    "authenticatedUser": "user@example.com"
  }
}
```
</details>

---

## ✅ Validation Rules

| Field | Validation |
|-------|------------|
| `email` | Required, valid email format |
| `password` | Required, minimum 6 characters |

---

## 🔒 Security Implementation

| Feature | Implementation |
|---------|----------------|
| **Password Hashing** | BCrypt with default strength |
| **JWT Tokens** | HS256 signature, 30-min expiry |
| **Session Management** | Stateless (no server-side sessions) |
| **CSRF Protection** | Disabled (appropriate for REST APIs) |
| **Endpoint Protection** | All endpoints except `/api/auth/**` require JWT |

---

## 📧 Email Templates

The application sends **professionally styled HTML emails** with:
- Gradient header design
- Call-to-action button
- Fallback plain text link
- Responsive layout

---

## 🐛 Troubleshooting

### Email Not Sending

1. **Check console logs** for error messages
2. **Gmail users**: You must use an [App Password](https://myaccount.google.com/apppasswords), not your regular password
3. **Development fallback**: If email fails, the verification link is logged to console:
   ```
   DEVELOPMENT MODE - Verification link: http://localhost:8080/api/auth/verify?token=...
   ```

### Database Connection Issues

Ensure PostgreSQL is running and the database exists:
```bash
psql -U postgres -c "SELECT 1 FROM pg_database WHERE datname = 'custom_auth_db'"
```

---

## 📄 API Response Format

All responses follow a consistent structure:

```json
{
  "success": boolean,
  "message": "string",
  "data": object | null
}
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the MIT License.

---

<p align="center">
  Made with ❤️ using Spring Boot
</p>
