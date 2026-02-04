# Custom Authentication API

A Spring Boot application providing custom JWT-based authentication with email verification.

## Features

- ✅ **User Registration** with email verification
- ✅ **JWT Authentication** for secure API access
- ✅ **Email Verification** with unique, time-limited links (10 minutes)
- ✅ **Login Protection** - Users cannot login until verified
- ✅ **Auto-resend Verification** - Verification email resent on unverified login attempt
- ✅ **Rate Limiting** - 5-minute cooldown between verification email requests
- ✅ **Token Invalidation** - Old verification links become invalid when a new one is generated
- ✅ **Input Validation** - Email format, password length validation
- ✅ **H2 File-based Database** - Persistent storage (not in-memory)

## Tech Stack

- Spring Boot 4.0.2
- Spring Security 6
- Spring Data JPA
- JWT (jjwt)
- H2 Database (file-based)
- JavaMailSender
- Lombok

## Setup

### 1. Email Configuration

Update `src/main/resources/application.yaml` with your SMTP credentials:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com      # Replace with your email
    password: your-app-password          # Replace with your App Password
```

**Note:** For Gmail, you need to [generate an App Password](https://support.google.com/accounts/answer/185833).

### 2. Database

The application uses H2 file-based database. Data is stored in `./data/testdb`. No external database setup required.

### 3. Running the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`.

## API Endpoints

### Authentication

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "User registered successfully. Please check your email for verification.",
  "data": null
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (200 OK) - Verified User:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

**Response (403 Forbidden) - Unverified User:**
```json
{
  "success": false,
  "message": "Account not verified. A new verification email has been sent.",
  "data": null
}
```

**Response (429 Too Many Requests) - Cooldown Active:**
```json
{
  "success": false,
  "message": "Account not verified. Please check your email. You can request a new link after X minute(s).",
  "data": null
}
```

#### Verify Email
```http
GET /api/auth/verify?token={verification-token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Account verified successfully. You can now login.",
  "data": null
}
```

### Protected Endpoints

#### Test Endpoint
```http
GET /api/test
Authorization: Bearer {jwt-token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "You have accessed a protected endpoint successfully!",
  "data": {
    "authenticatedUser": "user@example.com"
  }
}
```

**Response (401 Unauthorized) - No/Invalid Token:**
```json
{
  "success": false,
  "message": "Authentication required. Please provide a valid JWT token.",
  "data": null
}
```

### H2 Console

Access the database console at:
```
http://localhost:8080/h2-console
```

**Connection details:**
- JDBC URL: `jdbc:h2:file:./data/testdb`
- Username: `sa`
- Password: `password`

## Validation Rules

| Field    | Rules                                |
|----------|--------------------------------------|
| Email    | Required, must be valid email format |
| Password | Required, minimum 6 characters       |

## Security Features

1. **JWT Authentication**: Tokens expire after 30 minutes
2. **Password Encryption**: BCrypt hashing
3. **Stateless Sessions**: No server-side session storage
4. **CSRF Disabled**: Appropriate for REST APIs
5. **Protected Endpoints**: All endpoints except `/api/auth/**` require authentication

## Error Responses

All API responses follow this format:

```json
{
  "success": boolean,
  "message": "string",
  "data": object | null
}
```

## Development Notes

- When email sending fails (e.g., invalid SMTP credentials), the verification link is logged to the console for testing purposes
- The H2 console is accessible without authentication for development convenience
- JWT secret and expiration are configurable in `application.yaml`

## Project Structure

```
src/main/java/com/example/custom_authentication/
├── config/
│   ├── JwtAuthenticationEntryPoint.java
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   └── TestController.java
├── dto/
│   ├── ApiResponse.java
│   ├── AuthResponse.java
│   ├── LoginRequest.java
│   └── RegisterRequest.java
├── entity/
│   └── User.java
├── exception/
│   ├── EmailCooldownException.java
│   ├── GlobalExceptionHandler.java
│   └── UserNotVerifiedException.java
├── filter/
│   └── JwtAuthenticationFilter.java
├── repository/
│   └── UserRepository.java
├── service/
│   ├── CustomUserDetailsService.java
│   ├── EmailService.java
│   └── UserService.java
├── util/
│   └── JwtUtil.java
└── CustomAuthenticationProjectApplication.java
```

## 📧 Email Troubleshooting

If you are not receiving emails, check the application logs in the console.

1.  **"Authentication Failed" Error**:
    -   This means your `username` or `password` in `application.yaml` is incorrect.
    -   **Gmail Users**: You CANNOT use your regular password. You MUST use an **App Password**.
        1.  Go to [Google Account Security](https://myaccount.google.com/security).
        2.  Enable **2-Step Verification**.
        3.  Search for **App Passwords**.
        4.  Create one for "Mail" and "Other (Custom name)".
        5.  Use that 16-character code as your password in `application.yaml`.

2.  **Development Mode Fallback**:
    -   If email sending fails, the application **automatically logs the verification link** to the console.
    -   Look for a log line starting with: `DEVELOPMENT MODE - Verification link: ...`
    -   Copy and open that link in your browser to verify your account manually.

## 🔄 Resend Verification

You can resend the verification email by making a POST request:
`POST /api/auth/resend-verification?email=user@example.com`
