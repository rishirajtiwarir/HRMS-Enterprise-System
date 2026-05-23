package com.enterprise.hrms.config;

import com.enterprise.hrms.entity.*;
import com.enterprise.hrms.repository.DepartmentRepository;
import com.enterprise.hrms.repository.EmployeeRepository;
import com.enterprise.hrms.repository.RoleRepository;
import com.enterprise.hrms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Startup class to seed basic roles, departments, and admin/hr credentials into MySQL database.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Seed Roles
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(RoleName.ROLE_ADMIN));
            roleRepository.save(new Role(RoleName.ROLE_HR));
            roleRepository.save(new Role(RoleName.ROLE_EMPLOYEE));
        }

        // 2. Seed Departments
        Department itDept = null;
        Department hrDept = null;
        Department finDept = null;
        if (departmentRepository.count() == 0) {
            itDept = new Department();
            itDept.setName("Information Technology");
            itDept.setCode("IT");
            itDept.setDescription("Software engineering, operations, tech support");
            departmentRepository.save(itDept);

            hrDept = new Department();
            hrDept.setName("Human Resources");
            hrDept.setCode("HR");
            hrDept.setDescription("Recruitment, payroll, employee welfare");
            departmentRepository.save(hrDept);

            finDept = new Department();
            finDept.setName("Finance & Accounting");
            finDept.setCode("FIN");
            finDept.setDescription("Budgeting, tax filing, corporate accounts");
            departmentRepository.save(finDept);
        } else {
            itDept = departmentRepository.findByCode("IT").orElse(null);
            hrDept = departmentRepository.findByCode("HR").orElse(null);
        }

        // 3. Seed Demo Users
        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).orElse(null);
            Role hrRole = roleRepository.findByName(RoleName.ROLE_HR).orElse(null);
            Role employeeRole = roleRepository.findByName(RoleName.ROLE_EMPLOYEE).orElse(null);

            // A. Create Admin Employee & User
            Employee adminEmployee = new Employee();
            adminEmployee.setFirstName("System");
            adminEmployee.setLastName("Admin");
            adminEmployee.setEmail("admin@enterprise.com");
            adminEmployee.setPhone("+1-555-0100");
            adminEmployee.setDateOfBirth(LocalDate.of(1990, 5, 15));
            adminEmployee.setJoiningDate(LocalDate.of(2020, 1, 1));
            adminEmployee.setDesignation("System Administrator");
            adminEmployee.setSalary(100000.0);
            adminEmployee.setDepartment(itDept);
            adminEmployee.setStatus(EmployeeStatus.ACTIVE);

            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEmail("admin@enterprise.com");
            adminUser.setActive(true);
            
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);
            adminRoles.add(employeeRole); // Admins are also employees
            adminUser.setRoles(adminRoles);
            
            adminUser.setEmployee(adminEmployee);
            adminEmployee.setUser(adminUser);
            employeeRepository.save(adminEmployee);

            // B. Create HR Manager Employee & User
            Employee hrEmployee = new Employee();
            hrEmployee.setFirstName("Sarah");
            hrEmployee.setLastName("Jenkins");
            hrEmployee.setEmail("hr@enterprise.com");
            hrEmployee.setPhone("+1-555-0101");
            hrEmployee.setDateOfBirth(LocalDate.of(1992, 8, 20));
            hrEmployee.setJoiningDate(LocalDate.of(2021, 6, 1));
            hrEmployee.setDesignation("HR Lead");
            hrEmployee.setSalary(75000.0);
            hrEmployee.setDepartment(hrDept);
            hrEmployee.setStatus(EmployeeStatus.ACTIVE);

            User hrUser = new User();
            hrUser.setUsername("hr_manager");
            hrUser.setPassword(passwordEncoder.encode("hr123"));
            hrUser.setEmail("hr@enterprise.com");
            hrUser.setActive(true);

            Set<Role> hrRoles = new HashSet<>();
            hrRoles.add(hrRole);
            hrRoles.add(employeeRole);
            hrUser.setRoles(hrRoles);

            hrUser.setEmployee(hrEmployee);
            hrEmployee.setUser(hrUser);
            employeeRepository.save(hrEmployee);

            // C. Create Standard Employee
            Employee standardEmployee = new Employee();
            standardEmployee.setFirstName("John");
            standardEmployee.setLastName("Doe");
            standardEmployee.setEmail("john@enterprise.com");
            standardEmployee.setPhone("+1-555-0102");
            standardEmployee.setDateOfBirth(LocalDate.of(1995, 12, 10));
            standardEmployee.setJoiningDate(LocalDate.of(2023, 3, 15));
            standardEmployee.setDesignation("Software Engineer");
            standardEmployee.setSalary(60000.0);
            standardEmployee.setDepartment(itDept);
            standardEmployee.setStatus(EmployeeStatus.ACTIVE);

            User standardUser = new User();
            standardUser.setUsername("john_doe");
            standardUser.setPassword(passwordEncoder.encode("emp123"));
            standardUser.setEmail("john@enterprise.com");
            standardUser.setActive(true);

            Set<Role> standardRoles = new HashSet<>();
            standardRoles.add(employeeRole);
            standardUser.setRoles(standardRoles);

            standardUser.setEmployee(standardEmployee);
            standardEmployee.setUser(standardUser);
            employeeRepository.save(standardEmployee);
        }
    }
}
