# 📈 Brokerage Firm Stock Order Management API

A RESTful API for managing stock orders in a brokerage firm. Employees can create, list, and cancel stock orders on behalf of customers.

## 🎯 Project Overview

This backend service enables brokerage firm employees to manage customer stock trading operations. The system handles order creation, tracks customer assets (stocks and TRY balance), and provides admin controls for order matching.

### Key Features

- ✅ **Order Management**: Create BUY/SELL orders with PENDING status
- ✅ **Order Filtering**: List orders by customer and date range
- ✅ **Order Cancellation**: Cancel only PENDING orders
- ✅ **Asset Tracking**: View customer TRY balance and stock holdings
- ✅ **Balance Validation**: Automatic checking of available funds/stocks
- ✅ **JWT Authentication**: Secure token-based authentication
- ✅ **Role-Based Access**: Admin and Customer roles
- ✅ **Order Matching**: Admin can approve/match pending orders (Bonus Feature)

## 🏗️ Architecture

### Tech Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: H2 (in-memory)
- **ORM**: JPA/Hibernate
- **Security**: Spring Security + JWT
- **Testing**: JUnit 5 + Mockito
- **Build Tool**: Maven / Gradle

### Database Schema

**Customers Table**
```sql
- id (PK)
- username (unique)
- password (encrypted)
- role (ADMIN/CUSTOMER)
```

**Assets Table**
```sql
- id (PK)
- customerId (FK)
- assetName (TRY, AAPL, TSLA, etc.)
- size (total amount)
- usableSize (available amount)
```

**Orders Table**
```sql
- id (PK)
- customerId (FK)
- assetName (stock symbol)
- orderSide (BUY/SELL)
- size (quantity)
- price (per unit)
- status (PENDING/MATCHED/CANCELED)
- createDate
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+ or Gradle 7.0+

### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd brokerage-api
```

2. **Build the project**

Using Maven:
```bash
mvn clean install
```

Using Gradle:
```bash
./gradlew build
```

3. **Run the application**

Using Maven:
```bash
mvn spring-boot:run
```

Using Gradle:
```bash
./gradlew bootRun
```

4. **Access the application**
- API Base URL: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`
    - JDBC URL: `jdbc:h2:mem:brokeragedb`
    - Username: `sa`
    - Password: (leave empty)

### Running Tests

```bash
# Maven
mvn test

# Gradle
./gradlew test
```

## 📚 API Documentation

### Default Users

The application initializes with the following users:

| Username | Password | Role | Initial Balance |
|----------|----------|------|----------------|
| admin | admin123 | ADMIN | N/A |
| customer1 | password123 | CUSTOMER | 100,000 TRY + 10 AAPL stocks |
| customer2 | password123 | CUSTOMER | 50,000 TRY |

### Authentication

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "customer1",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "customer1",
  "role": "CUSTOMER",
  "customerId": 2
}
```

### Order Endpoints

#### Create Order
```http
POST /api/orders
Authorization: Bearer {token}
Content-Type: application/json

{
  "customerId": 2,
  "assetName": "AAPL",
  "orderSide": "BUY",
  "size": 10,
  "price": 150.50
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "customerId": 2,
  "assetName": "AAPL",
  "orderSide": "BUY",
  "size": 10,
  "price": 150.50,
  "status": "PENDING",
  "createDate": "2025-11-16T14:30:00"
}
```

#### List Orders
```http
GET /api/orders?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59
Authorization: Bearer {token}
```

- **Customer**: Can only view their own orders
- **Admin**: Must provide `customerId` parameter

#### Cancel Order
```http
DELETE /api/orders/{orderId}
Authorization: Bearer {token}
```

**Rules:**
- Only PENDING orders can be cancelled
- Refunds TRY or stocks back to usableSize

### Asset Endpoints

#### List Assets
```http
GET /api/assets
Authorization: Bearer {token}
```

- **Customer**: Views their own assets
- **Admin**: Must provide `customerId` parameter

**Response:**
```json
[
  {
    "id": 1,
    "customerId": 2,
    "assetName": "TRY",
    "size": 100000,
    "usableSize": 98747.50
  },
  {
    "id": 2,
    "customerId": 2,
    "assetName": "AAPL",
    "size": 10,
    "usableSize": 10
  }
]
```

### Admin Endpoints

#### Get Pending Orders
```http
GET /api/admin/orders/pending
Authorization: Bearer {admin_token}
```

#### Match Order
```http
POST /api/admin/orders/{orderId}/match
Authorization: Bearer {admin_token}
```

**What happens when matching:**
- Order status changes to MATCHED
- For BUY orders: TRY deducted, stocks added
- For SELL orders: Stocks deducted, TRY added
- Both `size` and `usableSize` updated accordingly

## 💡 Business Logic

### Creating a BUY Order

1. Customer wants to buy 10 AAPL stocks at 150 TRY each
2. System calculates total cost: 10 × 150 = 1,500 TRY
3. Checks if customer has 1,500 TRY in `usableSize`
4. If yes, creates PENDING order and locks 1,500 TRY (`usableSize` reduced)
5. Order waits for admin approval

### Creating a SELL Order

1. Customer wants to sell 5 AAPL stocks at 180 TRY each
2. Checks if customer has 5 AAPL in `usableSize`
3. If yes, creates PENDING order and locks 5 AAPL (`usableSize` reduced)
4. Order waits for admin approval

### Matching an Order

**BUY Order Match:**
- Transfer TRY from customer's TRY asset `size` (permanent deduction)
- Add purchased stocks to customer's stock asset (both `size` and `usableSize`)

**SELL Order Match:**
- Remove stocks from customer's stock asset `size` (permanent deduction)
- Add TRY revenue to customer's TRY asset (both `size` and `usableSize`)

### Canceling an Order

- Only PENDING orders can be cancelled
- Refunds locked amount back to `usableSize`
- Order status changes to CANCELED

## 🧪 Testing

The project includes comprehensive unit tests:

### Test Coverage

- **OrderServiceTest**: Order creation, cancellation, balance validation
- **AssetServiceTest**: Asset listing
- **OrderControllerTest**: API endpoint testing

### Example Test Scenarios

✅ Create BUY order with sufficient balance  
✅ Create BUY order with insufficient balance (fails)  
✅ Create SELL order with sufficient stocks  
✅ Create SELL order with insufficient stocks (fails)  
✅ Cancel PENDING order successfully  
✅ Cancel MATCHED order (fails)  
✅ Match order as admin

## 🔒 Security

### Authentication Flow

1. User sends credentials to `/api/auth/login`
2. Server validates and returns JWT token
3. Client includes token in `Authorization: Bearer {token}` header
4. Server validates token and extracts user info for each request

### Authorization Rules

- **Public**: `/api/auth/login`, `/h2-console`
- **Authenticated**: All other endpoints require valid JWT
- **Customer Role**: Can only access their own data
- **Admin Role**: Can access all customer data + admin endpoints

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/brokerage/
│   │   ├── BrokerageApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   └── DataInitializer.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── OrderController.java
│   │   │   ├── AssetController.java
│   │   │   └── AdminController.java
│   │   ├── service/
│   │   │   ├── OrderService.java
│   │   │   ├── AssetService.java
│   │   │   ├── AuthService.java
│   │   │   └── AdminService.java
│   │   ├── repository/
│   │   │   ├── CustomerRepository.java
│   │   │   ├── OrderRepository.java
│   │   │   └── AssetRepository.java
│   │   ├── model/
│   │   │   ├── Customer.java
│   │   │   ├── Order.java
│   │   │   ├── Asset.java
│   │   │   ├── OrderSide.java
│   │   │   └── OrderStatus.java
│   │   ├── dto/
│   │   ├── security/
│   │   └── exception/
│   └── resources/
│       └── application.yml
└── test/
    └── java/com/brokerage/
        ├── service/
        └── controller/
```

## 🎁 Bonus Features

### ✅ Bonus 1: Customer Authentication
- Separate login endpoint for customers
- JWT token-based authentication
- Customers can only access their own data
- Admin can access all data

### ✅ Bonus 2: Order Matching
- Admin endpoint to approve/match orders
- Automatic asset balance updates
- Status transition: PENDING → MATCHED

## 🐛 Error Handling

The API returns appropriate HTTP status codes and error messages:

- **400 Bad Request**: Invalid input, insufficient balance
- **401 Unauthorized**: Missing or invalid token
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found

Example error response:
```json
{
  "error": "Insufficient TRY balance"
}
```

## 📝 Configuration

### application.yml

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:brokeragedb
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: create-drop

jwt:
  secret: YourSuperSecretKeyForJWTTokenGeneration
  expiration: 86400000  # 24 hours
```


## 🐛 Deployment

The application is deployed and can be accessed at:

- Host: 141.144.228.239
- Port: 8080
- Environment: Test


## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is for educational/interview purposes.

## 👨‍💻 Author

Brokerage Firm Challenge

---

**For any questions or issues, please contact the development team.**
