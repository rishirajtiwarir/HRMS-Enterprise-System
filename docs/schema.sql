-- =========================================================================
-- ENTERPRISE EMPLOYEE & LEAVE MANAGEMENT SYSTEM (HRMS) DATABASE INITIALIZATION
-- DATABASE DIALECT: MySQL
-- =========================================================================

CREATE DATABASE IF NOT EXISTS hrms_db;
USE hrms_db;

-- -------------------------------------------------------------------------
-- DROP TABLES IF EXIST (Order respects foreign key dependencies)
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS payrolls;
DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS attendances;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS roles;

-- -------------------------------------------------------------------------
-- CREATE TABLES
-- -------------------------------------------------------------------------

-- 1. Roles table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) UNIQUE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Departments table
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    code VARCHAR(255) UNIQUE NOT NULL,
    description TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Employees table
CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(255) DEFAULT NULL,
    date_of_birth DATE DEFAULT NULL,
    joining_date DATE NOT NULL,
    designation VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    profile_image VARCHAR(255) DEFAULT NULL,
    salary DOUBLE NOT NULL,
    department_id BIGINT DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Users table (Authentication)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    employee_id BIGINT DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. User-Roles Join table (ManyToMany)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Attendances table
CREATE TABLE attendances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    check_in TIME DEFAULT NULL,
    check_out TIME DEFAULT NULL,
    status VARCHAR(30) NOT NULL,
    work_hours DOUBLE DEFAULT NULL,
    employee_id BIGINT NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Leave Requests table
CREATE TABLE leave_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    leave_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT DEFAULT NULL,
    created_at DATE NOT NULL,
    employee_id BIGINT NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Payrolls table
CREATE TABLE payrolls (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pay_period_start DATE NOT NULL,
    pay_period_end DATE NOT NULL,
    basic_salary DOUBLE NOT NULL,
    allowances DOUBLE DEFAULT 0.0,
    deductions DOUBLE DEFAULT 0.0,
    net_salary DOUBLE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    payment_date DATE DEFAULT NULL,
    payslip_pdf_path VARCHAR(255) DEFAULT NULL,
    employee_id BIGINT NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Notifications table
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(500) NOT NULL,
    type VARCHAR(30) NOT NULL DEFAULT 'INFO',
    read_status BOOLEAN NOT NULL DEFAULT FALSE,
    employee_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------------------
-- SEED DATA
-- -------------------------------------------------------------------------

-- 1. Seed Roles
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_HR');
INSERT INTO roles (name) VALUES ('ROLE_EMPLOYEE');

-- 2. Seed Departments
INSERT INTO departments (name, code, description) VALUES 
('Information Technology', 'IT', 'Software engineering, operations, tech support'),
('Human Resources', 'HR', 'Recruitment, payroll, employee welfare'),
('Finance & Accounting', 'FIN', 'Budgeting, tax filing, corporate accounts');

-- Note: In a live system, passwords are encrypted using BCrypt ($2a$10$...).
-- The following seed statements show references. Spring Boot will execute
-- DatabaseInitializer on start to auto-create and encode these users dynamically.
