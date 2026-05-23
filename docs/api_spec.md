# Enterprise HRMS - API Specification

All API endpoints are mapped to the context path `/api/v1`.

---

## 1. Authentication Module (`/api/v1/auth`)

### 1.1 User Login
- **URL**: `/login`
- **Method**: `POST`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY4NDc5ODQwMCwiZXhwIjoxNjg0Nzk5MzAwfQ...",
    "type": "Bearer",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY4NDc5ODQwMCwiZXhwIjoxNjg1NDA0ODAwfQ...",
    "id": 1,
    "username": "admin",
    "email": "admin@enterprise.com",
    "roles": ["ROLE_ADMIN", "ROLE_EMPLOYEE"],
    "employeeId": 1
  }
  ```

### 1.2 User Registration
- **URL**: `/register`
- **Method**: `POST`
- **Headers**: `Content-Type: application/json`, `Authorization: Bearer <access_token>` (Admin/HR Only)
- **Request Body**:
  ```json
  {
    "username": "john_doe",
    "password": "emp123password",
    "email": "john.doe@enterprise.com",
    "roles": ["employee"],
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+1-555-9000",
    "dateOfBirth": "1995-10-12",
    "joiningDate": "2026-05-23",
    "designation": "Software Engineer",
    "salary": 65000.0,
    "departmentId": 1
  }
  ```
- **Success Response (201 Created)**:
  ```json
  {
    "message": "Employee and user account registered successfully!"
  }
  ```

### 1.3 Refresh Access Token
- **URL**: `/refresh-token`
- **Method**: `POST`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY4NDc5ODQwMCwiZXhwIjoxNjg1NDA0ODAwfQ..."
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY4NDgwMDAwMCwiZXhwIjoxNjg0ODAwOTAwfQ...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY4NDgwMDAwMCwiZXhwIjoxNjg1NDA2NDAwfQ...",
    "tokenType": "Bearer"
  }
  ```

---

## 2. Employee Module (`/api/v1/employees`)

### 2.1 Get Employees (Paginated, Searchable, Sorted)
- **URL**: `/`
- **Method**: `GET`
- **Query Parameters**:
  - `search` (Optional): Filter by first name, last name, or email.
  - `page` (Default: 0): Page index.
  - `size` (Default: 10): Items per page.
  - `sortBy` (Default: `id`): Sort column.
  - `sortDir` (Default: `asc`): Sort direction (`asc` or `desc`).
- **Headers**: `Authorization: Bearer <access_token>`
- **Success Response (200 OK)**:
  ```json
  {
    "content": [
      {
        "id": 3,
        "firstName": "John",
        "lastName": "Doe",
        "email": "john@enterprise.com",
        "phone": "+1-555-0102",
        "dateOfBirth": "1995-12-10",
        "joiningDate": "2023-03-15",
        "designation": "Software Engineer",
        "status": "ACTIVE",
        "profileImage": null,
        "salary": 60000.0,
        "departmentId": 1,
        "departmentName": "Information Technology",
        "username": "john_doe",
        "userRole": "ROLE_EMPLOYEE"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": { "sorted": true, "unsorted": false, "empty": false }
    },
    "totalElements": 3,
    "totalPages": 1,
    "size": 10,
    "number": 0,
    "first": true,
    "last": true
  }
  ```

### 2.2 Upload Profile Image
- **URL**: `/{id}/image`
- **Method**: `POST`
- **Headers**: `Authorization: Bearer <access_token>`, `Content-Type: multipart/form-data`
- **Form Data Parameters**:
  - `file` (Multipart file: jpeg/png)
- **Success Response (200 OK)**:
  ```json
  {
    "id": 3,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@enterprise.com",
    "profileImage": "/api/v1/employees/images/employee_3_1716499992.png",
    "salary": 60000.0,
    "departmentId": 1,
    "departmentName": "Information Technology"
  }
  ```

---

## 3. Leave Module (`/api/v1/leaves`)

### 3.1 Apply for Leave
- **URL**: `/request`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "leaveType": "VACATION",
    "startDate": "2026-06-01",
    "endDate": "2026-06-05",
    "reason": "Family vacation trip"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "id": 1,
    "employeeId": 3,
    "employeeName": "John Doe",
    "leaveType": "VACATION",
    "startDate": "2026-06-01",
    "endDate": "2026-06-05",
    "reason": "Family vacation trip",
    "status": "PENDING",
    "rejectionReason": null,
    "createdAt": "2026-05-23"
  }
  ```

### 3.2 Resolve Leave Request (Approve/Reject)
- **URL**: `/{id}/resolve`
- **Method**: `PUT`
- **Query Parameters**:
  - `status`: `APPROVED` or `REJECTED`
  - `rejectionReason` (Required if status is `REJECTED`): Reason for rejection.
- **Success Response (200 OK)**:
  ```json
  {
    "id": 1,
    "employeeId": 3,
    "employeeName": "John Doe",
    "leaveType": "VACATION",
    "startDate": "2026-06-01",
    "endDate": "2026-06-05",
    "status": "APPROVED",
    "rejectionReason": null
  }
  ```

---

## 4. Attendance Module (`/api/v1/attendance`)

### 4.1 Employee Check-In
- **URL**: `/check-in`
- **Method**: `POST`
- **Success Response (200 OK)**:
  ```json
  {
    "id": 15,
    "employeeId": 3,
    "employeeName": "John Doe",
    "date": "2026-05-23",
    "checkIn": "09:12:45",
    "checkOut": null,
    "status": "PRESENT",
    "workHours": null
  }
  ```

---

## 5. Payroll Module (`/api/v1/payroll`)

### 5.1 Generate Bulk Payroll
- **URL**: `/generate-bulk`
- **Method**: `POST`
- **Query Parameters**:
  - `startDate`: `2026-05-01`
  - `endDate`: `2026-05-31`
- **Success Response (200 OK)**:
  ```json
  [
    {
      "id": 1,
      "employeeId": 3,
      "employeeName": "John Doe",
      "designation": "Software Engineer",
      "payPeriodStart": "2026-05-01",
      "payPeriodEnd": "2026-05-31",
      "basicSalary": 60000.0,
      "allowances": 6000.0,
      "deductions": 3000.0,
      "netSalary": 63000.0,
      "status": "PENDING",
      "paymentDate": null,
      "payslipPdfPath": null
    }
  ]
  ```
