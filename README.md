# Virtual Card Platform

## Overview
A modern, reactive microservices-based platform for managing virtual payment cards and transactions. Built with a focus on scalability, reliability, and maintainability using cutting-edge technologies and best practices.

## Architecture & Design Choices

### Microservices Architecture
The platform is divided into specialized, autonomous services:

- **card-service**: Core service for card management operations
- **transaction-service**: Handles all transaction-related operations
- **card-aggregate-service**: Aggregates card-related data and provides composite operations
- **discovery-server**: Service registry and discovery
- **Common modules**: Shared libraries for cross-cutting concerns

### Technology Stack

#### Core Technologies
- **Java 21**: Latest LTS version with modern language features
- **Spring Boot**: Application framework
- **Spring WebFlux**: Reactive web framework
- **Project Reactor**: Reactive programming library
- **MySQL**: Primary database (with reactive drivers)
- **jOOQ**: Type-safe SQL query builder
- **Maven**: Build and dependency management

#### Supporting Libraries
- **Lombok**: Reduces boilerplate code
- **TestContainers**: Integration testing with containerized dependencies
- **Spring Cloud**: Microservices coordination

### Key Features

#### Card Management 💳
- Create virtual cards with initial balance
- Retrieve card information
- Validate card status and coverage
- Update card balances
- Check balance coverage for transactions

#### Advanced Security & Control 🔒
- **Optimistic Locking**
  - Version field in Card entity for concurrent updates
  - Automatic version increment on updates
  - DataChangedException handling for concurrent modifications
  - Configured in jOOQ DSLContext settings

- **Rate Limiting**
  - 5 spend operations per minute limit
  - Prevents excessive transaction attempts
  - Built-in protection against abuse

- **Card Status Management**
  - ACTIVE: Default status, allows all operations
  - BLOCKED: Restricted operations, no transactions
  - Status-based transaction validation

#### Transaction Processing 💸
- Process financial transactions
- Transaction history tracking
- Real-time transaction validation
- Balance verification
- Transaction history with pagination

#### Concurrency Control 🔄
* **Optimistic Locking Implementation**
    - Version tracking for each card record
    - Automatic version increment on updates
    - Conflict detection and resolution
    - Tested with concurrent update scenarios

* **Rate Limiting Strategy**
    - Per-card operation tracking
    - Rolling window implementation
    - Configurable limits and windows
    - Graceful rejection handling

#### Platform Features ⚙️
- Service discovery and registration
- Reactive endpoints for better scalability
- Type-safe database operations with jOOQ
- Comprehensive error handling
- Centralized configuration

## Project Structure

## Technical Highlights

### Reactive Programming
- Non-blocking I/O operations
- Reactive streams for data flow
- Better resource utilization
- Enhanced scalability

### Database Access
- Reactive database operations
- Type-safe SQL with jOOQ
- Connection pooling
- Transaction management

### Testing Strategy
- Unit tests for business logic
- Integration tests with TestContainers
- End-to-end testing support
- Shared testing utilities

## Getting Started

### Prerequisites
- JDK 21
- Maven 3.8+
- Docker (for local development)
- MySQL 8.0+

### Database Setup (Required for Compilation)
This project uses jOOQ for type-safe SQL queries, which requires a live database connection during compilation to generate the necessary entity classes.

1. Create the database and tables:
```bash
# Navigate to database directory
cd database

# Using MySQL CLI (adjust credentials as needed)
mysql -u root -p < schema.sql

# Alternative: Using Docker
docker exec -i mysql-virtualcard mysql -uroot -proot virtualcard < schema.sql
```

### Building the Project
mvn clean install

### Running Services
1. Start the discovery server:
```bash
    cd discovery-server 
    mvn spring-boot:run
```

2. Start core services:
```bash
   cd card-service 
   mvn spring-boot:run
```

### Development Setup
1. Clone the repository
2. Import as Maven project
3. Run database migrations
4. Start required services

## Configuration

### Service Discovery
- Automatic service registration
- Dynamic service discovery
- Load balancing support

### Database
- Reactive connection pool settings
- Failover configuration
- Connection timeouts

### Monitoring
- Actuator endpoints
- Metrics collection
- Health checks


## Learning Strategy

### First Steps with jOOQ

#### Why jOOQ? 🤔
* **Type Safety**
    - Compile-time SQL validation
    - Automated code generation from schema
    - Refactoring-friendly codebase

* **Performance**
    - Direct SQL optimization
    - Reduced overhead compared to JPA
    - Fine-grained query control

* **Developer Experience**
    - SQL-like fluent API
    - IDE auto-completion support
    - Excellent debugging capabilities

#### Learning Path 📚

1. **Foundation Phase**
    - [x] Official jOOQ documentation deep dive
    - [x] Basic CRUD operations
    - [x] Understanding code generation
    - [x] Database schema setup

2. **Integration Phase**
    - [x] Spring Boot integration
    - [x] Reactive adapters setup
    - [x] Transaction management
    - [x] Connection pooling

3. **Advanced Concepts**
    - [x] Complex queries
    - [x] Custom converters
    - [x] Optimistic locking
    - [x] Batch operations

#### Challenges & Solutions 💡

#### Key Learnings 🎯

1. **Technical Insights**
   ```
   - Type-safe queries are better than String-based queries
   - Schema-first development is powerful
   - Version control for generated code
   ```

2. **Best Practices**
   ```
   - Centralize database configuration
   - Implement proper error handling
   - Use version control for schema
   - Regular schema backups
   ```

### Resources Used 📚

#### Official Documentation
- [jOOQ Documentation](https://www.jooq.org/doc/latest/manual/)
- [Spring Boot Integration Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html#data.sql.jooq)

#### Community Resources
- Stack Overflow discussions
- GitHub examples
- Medium
- Baeldung

## Future Learning Goals

### Security Enhancements
- Implement Spring Security with JWT authentication
- Add role-based access control
- API key validation for service-to-service communication

### Infrastructure Improvements
- Set up Spring Cloud Config Server for centralized configuration
- Add circuit breakers for resilience

### Performance Optimizations
- Implement Redis caching for card spend limits
- Replace in-memory map with Redis for rate limiting

### Feature Enhancements
- Implement proper card number generation using Luhn algorithm
- Add card expiration date handling
- Implement card activation/deactivation workflow
- Add transaction categorization

### Event-Driven Architecture
- Implement Apache Kafka for asynchronous operations
    - Card and Transaction creation
    - Balance updates
- Add dead letter queue handling

