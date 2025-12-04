## Service Dog Registry App

### Overview
System for managing police dog records, including

* **Dog Management** -- Lifecycle management from acquisition to retirement
* **Supplier Tracing** -- Maintain relationship with dog training suppliers
* **Search & Filtering** -- Search capabilities with multiple criteria
* **Auditing**
* **Soft Delete**
* **Optimistic Locking**
* **Validation**
* **Error Handling**
* **H2 Database** with some sample data

### Build the project
```text
mvn clean package
```

### Running the project
```text
mvn spring-boot:run
```

### Accessing Swagger UI

![Swagger UI](http://localhost:8080/swagger-ui/index.html)

### Tech Stack

#### Backend
* Java 21
* Spring Boot 3.5.8
* Spring Data JPA
* Hibernate
* H2 Database

#### Libraries & Tools
* MapStruct
* Lombok
* OpenAPI
* Jackson
* Bean Validation

#### Testing
* JUnit5
* Mockito
* SpringBootTest
* MockMvc

### What's not covered
* DB Index on search fields
* Caching
* OpenAPI Spec doesn't cover error responses
* Some refactoring of code.
* Returning ProblemDetail instead of raw error responses for 4XX and 5XX