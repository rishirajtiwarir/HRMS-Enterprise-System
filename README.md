# Enterprise Employee & Leave Management System (HRMS)

A full-stack, enterprise-grade Employee Management & Leave Management System (HRMS) designed with a clean layered architecture, featuring a Java Spring Boot backend and an Angular Material frontend. Ideal for final-year projects, developer portfolios, and resume showcases.

---

## 🛠️ Technology Stack

### Backend
- **Java**: Version 21
- **Spring Boot**: 3.2.x
- **Spring Security**: Stateless configuration with JSON Web Tokens (JWT)
- **Database**: MySQL (configured with Hibernate ORM)
- **API Documentation**: Swagger UI (OpenAPI v3)
- **Boilerplate Reduction**: Lombok

### Frontend
- **Angular**: Version 18 (Standalone architecture)
- **UI Styling**: Angular Material UI, CSS Variables (HSL Color Systems)
- **State & Reactive Logic**: RxJS, Reactive Forms
- **Themes**: Live Dark/Light Mode toggle

---

## 📂 Project Architecture & Code Structure

### Backend Layered Architecture
- `com.enterprise.hrms.entity`: JPA mappings (with audit fields, validations, and constructors).
- `com.enterprise.hrms.repository`: Database CRUD interfaces (Spring Data JPA).
- `com.enterprise.hrms.dto`: Structured request/response models.
- `com.enterprise.hrms.mapper`: Clean, direct Entity-to-DTO conversion mappers.
- `com.enterprise.hrms.service`: Interfaces declaring business actions.
- `com.enterprise.hrms.service.impl`: Class implementations managing transactional code.
- `com.enterprise.hrms.controller`: Secure REST endpoints mapping parameters.
- `com.enterprise.hrms.security`: Handles JWT validation filters, principal conversion, and password hashes.
- `com.enterprise.hrms.exception`: Transmits clean JSON error details back to the client using a Global Exception Handler.
- `com.enterprise.hrms.config`: Swagger definitions and Database seeding runners.

### Frontend Standalone Layout
- `/src/app/core/`: Contains JWT interceptors, Route guards, and state services (Authentication, Snackbars).
- `/src/app/features/`: Contains lazy-loaded business pages:
  - `auth/login`: Form layout validation.
  - `dashboard`: Responsive analytics grids and departmental budgets.
  - `employee`: CRUD directories and detail profiles.
  - `leave-management`: Application submission and HR approval centers.
  - `attendance`: Direct daily check-in/out triggers and log sheets.
  - `payroll`: bulk salary cycle generations and payslip downloads.

---

## 🚀 Step-by-Step Setup Guide

### Prerequisites
Before running, ensure you have the following installed on your machine:
- **Java SE Development Kit (JDK)**: Version 21 or higher.
- **Node.js**: Version 18.x or 20.x (with NPM).
- **MySQL Server**: Running locally on port 3306.
- **Angular CLI**: Install globally using `npm install -g @angular/cli`.

---

### Step 1: Database Setup
1. Open your MySQL client (e.g., MySQL Workbench, phpMyAdmin, or terminal).
2. Create the target database:
   ```sql
   CREATE DATABASE hrms_db;
   ```
3. (Optional) Run the SQL initialization script located at `docs/schema.sql` to import the tables and roles, or let Hibernate's `ddl-auto=update` generate them automatically on startup.

---

### Step 2: Running the Spring Boot Backend
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Open `src/main/resources/application.properties` and verify your MySQL credentials:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=root     # Update this to match your MySQL password
   ```
3. Run the application using Maven:
   ```bash
   mvn clean spring-boot:run
   ```
   *Note: If you do not have Maven installed on your shell PATH, import the project as a Maven project inside **IntelliJ IDEA** or **Eclipse** and run the `HRMSApplication` class directly.*
4. The server will start on port `8080` (context path `/api/v1`).
5. Access the interactive API docs (Swagger UI) at:
   [http://localhost:8080/api/v1/swagger-ui/index.html](http://localhost:8080/api/v1/swagger-ui/index.html)

---

### Step 3: Running the Angular Frontend
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install all node dependencies:
   ```bash
   npm install
   ```
3. Start the Angular local development server:
   ```bash
   npm start
   ```
4. The application will start and be accessible in your browser at:
   [http://localhost:4200](http://localhost:4200)

---

## 🔑 Demo Login Credentials

Upon startup, the system automatically seeds three test accounts into the database with default configurations. Use these to test different permission levels:

| Role | Username | Password | Email | Access Permissions |
| :--- | :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `admin123` | `admin@enterprise.com` | Full access to directory, departments, payroll generation, and dashboards. |
| **HR Manager** | `hr_manager` | `hr123` | `hr@enterprise.com` | Can manage employees, approve/reject leaves, run payrolls, and view stats. |
| **Employee** | `john_doe` | `emp123` | `john@enterprise.com` | Can log daily check-ins/outs, apply for leaves, check balances, and download own payslips. |

---

## 📄 Documentation Artifacts (Inside `/docs`)
- **schema.sql**: Database table layouts and foreign-key configurations.
- **api_spec.md**: REST endpoint paths, request formats, and response JSON samples.
- **postman_collection.json**: Direct imports to Postman containing variable sets for rapid testing.
