"# E-Commerce Backend API

A comprehensive Spring Boot REST API for an e-commerce platform providing complete functionality for managing products, categories, users, shopping cart, orders, and authentication.

## 🚀 Features

- **User Authentication & Authorization** with JWT tokens
- **Product Management** - CRUD operations for products
- **Category Management** - Product categorization
- **Shopping Cart** - Add, update, remove items
- **Order Management** - Complete order processing
- **Address Management** - User address handling
- **Role-based Access Control** - Admin and User roles
- **Image Upload** - Product image management
- **API Documentation** - Interactive Swagger UI
- **Database Support** - MySQL for production, H2 for testing

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.4.5
- **Java Version**: 21
- **Database**: MySQL (Primary), H2 (Testing)
- **Security**: Spring Security with JWT
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **ORM**: Spring Data JPA
- **Build Tool**: Maven
- **Additional Libraries**:
  - Lombok for boilerplate code reduction
  - ModelMapper for object mapping
  - JWT for authentication tokens
  - Spring Boot DevTools for development

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+
- MySQL 8.0+ (for production)
- Git

## 🚀 Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/vskartheek/ecom-backend.git
cd ecom-backend
```

### 2. Database Setup
Create a MySQL database:
```sql
CREATE DATABASE mydb;
```

### 3. Configure Database
Update the database configuration in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. Build and Run
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

Once the application is running, you can access the interactive API documentation at:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/ecommerce/project/
│   │   ├── SbEcomApplication.java          # Main application class
│   │   ├── config/                         # Configuration classes
│   │   ├── controller/                     # REST controllers
│   │   │   ├── AddressController.java
│   │   │   ├── AuthController.java
│   │   │   ├── CartController.java
│   │   │   ├── CategoryController.java
│   │   │   ├── OrderController.java
│   │   │   └── ProductController.java
│   │   ├── exceptions/                     # Custom exceptions
│   │   ├── model/                         # Entity classes
│   │   ├── payload/                       # DTOs and request/response objects
│   │   ├── repositories/                  # Data access layer
│   │   ├── security/                      # Security configuration
│   │   ├── service/                       # Business logic layer
│   │   └── util/                         # Utility classes
│   └── resources/
│       ├── application.properties         # Application configuration
│       ├── static/                       # Static resources
│       └── templates/                    # Template files
└── test/                                 # Test classes
```

## 🔧 Configuration

### Environment Variables
You can override the default configuration using environment variables:

- `DB_URL`: Database URL
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `JWT_SECRET`: JWT secret key
- `JWT_EXPIRATION`: JWT token expiration time
- `SERVER_PORT`: Application port (default: 8080)

### Profiles
The application supports different profiles:
- `dev`: Development environment
- `prod`: Production environment
- `test`: Testing environment

## 🔑 Authentication

The API uses JWT-based authentication. To access protected endpoints:

1. Register or login through `/api/auth/login`
2. Include the JWT token in the Authorization header: `Bearer <token>`

## 📊 Database Schema

The application uses the following main entities:
- **User**: User account information
- **Role**: User roles (ADMIN, USER)
- **Category**: Product categories
- **Product**: Product information
- **Cart**: Shopping cart
- **CartItem**: Items in cart
- **Order**: Order information
- **OrderItem**: Items in order
- **Address**: User addresses
- **Payment**: Payment information

## 🧪 Testing

Run the test suite:
```bash
mvn test
```

## 🚀 Deployment

### Docker Deployment (Optional)
Create a Dockerfile in the project root:
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/sb-ecom-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:
```bash
docker build -t ecom-backend .
docker run -p 8080:8080 ecom-backend
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Kartheek VS**
- GitHub: [@vskartheek](https://github.com/vskartheek)

## 🐛 Issues

If you encounter any issues, please create an issue on the [GitHub repository](https://github.com/vskartheek/ecom-backend/issues).



⭐ If you find this project helpful, please give it a star!" 
