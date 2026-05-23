package com.enterprise.hrms.service.impl;

import com.enterprise.hrms.dto.*;
import com.enterprise.hrms.entity.*;
import com.enterprise.hrms.exception.BadRequestException;
import com.enterprise.hrms.exception.ResourceNotFoundException;
import com.enterprise.hrms.exception.TokenRefreshException;
import com.enterprise.hrms.repository.DepartmentRepository;
import com.enterprise.hrms.repository.EmployeeRepository;
import com.enterprise.hrms.repository.RoleRepository;
import com.enterprise.hrms.repository.UserRepository;
import com.enterprise.hrms.security.JwtTokenProvider;
import com.enterprise.hrms.security.UserPrincipal;
import com.enterprise.hrms.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        // Authenticate credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate Access and Refresh JWT Tokens
        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Use the username to sign the refresh token (or custom payload)
        String refreshToken = tokenProvider.generateTokenFromUsername(userPrincipal.getUsername());

        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new JwtResponse(
                jwt,
                refreshToken,
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                userPrincipal.getEmail(),
                roles,
                userPrincipal.getEmployeeId()
        );
    }

    @Override
    @Transactional
    public void registerUser(RegisterRequest registerRequest) {
        // Validate unique constraints
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email Address already in use!");
        }

        // Fetch department
        Department department = null;
        if (registerRequest.getDepartmentId() != null) {
            department = departmentRepository.findById(registerRequest.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + registerRequest.getDepartmentId()));
        }

        // Create and populate Employee entity
        Employee employee = new Employee();
        employee.setFirstName(registerRequest.getFirstName());
        employee.setLastName(registerRequest.getLastName());
        employee.setEmail(registerRequest.getEmail());
        employee.setPhone(registerRequest.getPhone());
        employee.setDateOfBirth(registerRequest.getDateOfBirth());
        employee.setJoiningDate(registerRequest.getJoiningDate());
        employee.setDesignation(registerRequest.getDesignation());
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setSalary(registerRequest.getSalary());
        employee.setDepartment(department);

        // Create User account credentials
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setActive(true);

        // Assign Roles
        Set<Role> roles = new HashSet<>();
        if (registerRequest.getRoles() == null || registerRequest.getRoles().isEmpty()) {
            Role userRole = roleRepository.findByName(RoleName.ROLE_EMPLOYEE)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: ROLE_EMPLOYEE"));
            roles.add(userRole);
        } else {
            registerRequest.getRoles().forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                                .orElseThrow(() -> new ResourceNotFoundException("Role not found: ROLE_ADMIN"));
                        roles.add(adminRole);
                        break;
                    case "hr":
                        Role hrRole = roleRepository.findByName(RoleName.ROLE_HR)
                                .orElseThrow(() -> new ResourceNotFoundException("Role not found: ROLE_HR"));
                        roles.add(hrRole);
                        break;
                    default:
                        Role empRole = roleRepository.findByName(RoleName.ROLE_EMPLOYEE)
                                .orElseThrow(() -> new ResourceNotFoundException("Role not found: ROLE_EMPLOYEE"));
                        roles.add(empRole);
                }
            });
        }
        user.setRoles(roles);

        // Map relationships
        user.setEmployee(employee);
        employee.setUser(user);

        // Saving the employee will cascade and save the user too
        employeeRepository.save(employee);
    }

    @Override
    public TokenRefreshResponse refreshAccessToken(TokenRefreshRequest tokenRefreshRequest) {
        String requestRefreshToken = tokenRefreshRequest.getRefreshToken();

        // Validate Refresh Token
        if (tokenProvider.validateToken(requestRefreshToken)) {
            String username = tokenProvider.getUsernameFromJWT(requestRefreshToken);
            
            // Check if user is active
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "User not found associated with this token"));
            
            if (!user.getActive()) {
                throw new TokenRefreshException(requestRefreshToken, "User account is deactivated");
            }

            // Issue new access token and a fresh refresh token
            String token = tokenProvider.generateTokenFromUsername(username);
            String newRefreshToken = tokenProvider.generateTokenFromUsername(username);

            return new TokenRefreshResponse(token, newRefreshToken);
        }

        throw new TokenRefreshException(requestRefreshToken, "Refresh token is expired or invalid!");
    }
}
