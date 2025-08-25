# Banking Application with Spring Boot & JWT Authentication

A comprehensive Banking Application built with Spring Boot (Java 17) featuring JWT Authentication, role-based access control, and secure banking operations.

## Features

### Authentication & Authorization
- **JWT Token-based Authentication**: Secure login with JSON Web Tokens
- **Role-based Access Control**: Two user roles - CUSTOMER and ADMIN
- **BCrypt Password Hashing**: Secure password storage
- **Security Configuration**: Spring Security with JWT filters

### Banking Operations
- **Account Management**: View account details and balance
- **Deposit**: Add money to accounts
- **Withdrawal**: Remove money from accounts (with balance validation)
- **Fund Transfer**: Transfer money between accounts
- **Transaction History**: View all account transactions
- **Balance Inquiry**: Check current account balance

### User Roles & Permissions
- **CUSTOMER**: Can manage their own account only
- **ADMIN**: Can manage all accounts in the system

### Architecture
- **Clean Architecture**: Controller → Service → Repository pattern
- **RESTful API**: Well-structured REST endpoints
- **Database Integration**: JPA/Hibernate with MySQL/PostgreSQL support
- **Exception Handling**: Global exception handler for consistent error responses

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security 6**
- **Spring Data JPA**
- **JWT (JSON Web Tokens)**
- **H2 Database** (for development)
- **MySQL/PostgreSQL** (for production)
- **Maven** (build tool)

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL/PostgreSQL (optional - H2 is configured by default)

### Running the Application

1. **Clone and build the project:**
   ```bash
   mvn clean install
   ```

2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

3. **Access the application:**
   - API Base URL: `http://localhost:8080`
   - H2 Console: `http://localhost:8080/h2-console`

### Default Test Accounts

The application initializes with sample accounts:

#### Admin Account
- **Username**: `admin`
- **Password**: `admin123`
- **Account Number**: `1000000001`
- **Balance**: $1,000,000.00

#### Customer Accounts
- **Username**: `customer1`
- **Password**: `customer123`
- **Account Number**: `2000000001`
- **Balance**: $5,000.00

- **Username**: `customer2`
- **Password**: `customer123`
- **Account Number**: `2000000002`
- **Balance**: $3,000.00

## API Endpoints

### Authentication Endpoints

#### Register New User
```http
POST /api/auth/signup
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "role": "CUSTOMER",
  "accountType": "SAVINGS"
}
```

#### Login
```http
POST /api/auth/signin
Content-Type: application/json

{
  "username": "customer1",
  "password": "customer123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 2,
  "username": "customer1",
  "email": "customer1@banking.com",
  "role": "CUSTOMER",
  "accountNumber": "2000000001"
}
```

### Account Operations

#### Get My Account Details
```http
GET /api/account/my-account
Authorization: Bearer <jwt-token>
```

#### Get Account Balance
```http
GET /api/account/{accountNumber}/balance
Authorization: Bearer <jwt-token>
```

#### Deposit Money
```http
POST /api/account/{accountNumber}/deposit
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "amount": 1000.00,
  "description": "Salary deposit"
}
```

#### Withdraw Money
```http
POST /api/account/{accountNumber}/withdraw
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "amount": 500.00,
  "description": "ATM withdrawal"
}
```

#### Transfer Money
```http
POST /api/account/{fromAccountNumber}/transfer
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "amount": 200.00,
  "description": "Transfer to friend",
  "toAccountNumber": "2000000002"
}
```

#### Get Transaction History
```http
GET /api/account/{accountNumber}/transactions
Authorization: Bearer <jwt-token>
```

### Admin Operations

#### Get All Accounts (Admin Only)
```http
GET /api/admin/accounts
Authorization: Bearer <admin-jwt-token>
```

## How JWT Security Works

### 1. Authentication Flow
1. **User Registration**: User registers with credentials
2. **Password Hashing**: Password is hashed using BCrypt
3. **User Login**: User provides username/password
4. **Token Generation**: JWT token is generated upon successful authentication
5. **Token Response**: Token is returned to client

### 2. Authorization Flow
1. **Token Inclusion**: Client includes JWT token in Authorization header
2. **Token Validation**: Server validates token signature and expiration
3. **User Extraction**: User details extracted from token
4. **Permission Check**: Role-based permissions checked
5. **Request Processing**: Request processed if authorized

### 3. JWT Token Structure
```
Header.Payload.Signature
```

- **Header**: Contains algorithm information
- **Payload**: Contains user information and claims
- **Signature**: Ensures token integrity

### 4. Security Features
- **Token Expiration**: Tokens expire after 24 hours
- **Secret Key**: Strong secret key for signing tokens
- **CORS Support**: Cross-origin requests supported
- **Stateless**: No server-side session storage

## Banking Operations Workflow

### 1. Deposit Operation
1. **Validation**: Check account exists and is active
2. **Authorization**: Verify user can access account
3. **Balance Update**: Add deposit amount to current balance
4. **Transaction Record**: Create DEPOSIT transaction record
5. **Response**: Return transaction details

### 2. Withdrawal Operation
1. **Validation**: Check account exists, is active, and has sufficient balance
2. **Authorization**: Verify user can access account
3. **Balance Check**: Ensure sufficient funds available
4. **Balance Update**: Subtract withdrawal amount from balance
5. **Transaction Record**: Create WITHDRAWAL transaction record
6. **Response**: Return transaction details

### 3. Transfer Operation
1. **Validation**: Check both accounts exist and are active
2. **Authorization**: Verify user can access source account
3. **Balance Check**: Ensure sufficient funds in source account
4. **Atomic Transaction**: 
   - Debit source account
   - Credit destination account
   - Create transaction records for both accounts
5. **Response**: Return success message

### 4. Transaction History
1. **Authorization**: Verify user can access account
2. **Query**: Retrieve transactions ordered by date (newest first)
3. **Response**: Return formatted transaction list

## Database Schema

### Users Table
- `id` (Primary Key)
- `username` (Unique)
- `email` (Unique)
- `password` (BCrypt hashed)
- `first_name`
- `last_name`
- `role` (CUSTOMER/ADMIN)
- `enabled`
- `created_at`
- `updated_at`

### Accounts Table
- `id` (Primary Key)
- `account_number` (Unique)
- `balance`
- `account_type` (SAVINGS/CHECKING/BUSINESS)
- `status` (ACTIVE/INACTIVE/SUSPENDED/CLOSED)
- `user_id` (Foreign Key)
- `created_at`
- `updated_at`

### Transactions Table
- `id` (Primary Key)
- `amount`
- `type` (DEPOSIT/WITHDRAWAL/TRANSFER_IN/TRANSFER_OUT)
- `description`
- `transaction_date`
- `balance_after`
- `account_id` (Foreign Key)
- `to_account_number` (for transfers)
- `from_account_number` (for transfers)

## Configuration

### Database Configuration (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:bankingdb  # H2 for development
    # url: jdbc:mysql://localhost:3306/banking_db  # MySQL for production
    username: sa
    password: password
  
  jpa:
    hibernate:
      ddl-auto: create-drop  # Use 'update' for production
    show-sql: true

jwt:
  secret: mySecretKey12345678901234567890123456789012345678901234567890
  expiration: 86400000  # 24 hours
```

### Security Configuration
- JWT token validation on every request
- Role-based method security
- CORS enabled for cross-origin requests
- Stateless session management

## Error Handling

The application includes comprehensive error handling:

- **Validation Errors**: Field-level validation with detailed messages
- **Authentication Errors**: Invalid credentials handling
- **Authorization Errors**: Access denied responses
- **Business Logic Errors**: Insufficient balance, account not found, etc.
- **Generic Errors**: Unexpected server errors

## Testing

### Manual Testing with cURL

#### 1. Register a new user:
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

#### 2. Login and get JWT token:
```bash
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer1",
    "password": "customer123"
  }'
```

#### 3. Use JWT token to make authenticated requests:
```bash
curl -X GET http://localhost:8080/api/account/my-account \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## Production Deployment

### Database Configuration
For production, update `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/banking_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
```

### Environment Variables
Set the following environment variables:
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `JWT_SECRET`: Strong secret key for JWT signing

### Security Considerations
1. Use HTTPS in production
2. Set strong JWT secret key
3. Configure proper CORS policies
4. Enable database connection pooling
5. Set up proper logging and monitoring
6. Implement rate limiting
7. Use environment-specific configurations

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.