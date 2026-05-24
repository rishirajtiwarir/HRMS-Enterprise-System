package com.enterprise.hrms.security;

import com.enterprise.hrms.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Custom UserDetails implementation containing profile and authority information of the logged-in user.
 */
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;

    @JsonIgnore
    private final String email;

    @JsonIgnore
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;
    private final Long employeeId;

    public UserPrincipal(Long id, String username, String email, String password, Collection<? extends GrantedAuthority> authorities, Long employeeId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.employeeId = employeeId;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .flatMap(role -> {
                    String name = role.getName().name();
                    if ("ROLE_SUPER_ADMIN".equals(name)) {
                        return java.util.stream.Stream.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"), new SimpleGrantedAuthority("ROLE_ADMIN"));
                    } else if ("ROLE_HR_MANAGER".equals(name)) {
                        return java.util.stream.Stream.of(new SimpleGrantedAuthority("ROLE_HR_MANAGER"), new SimpleGrantedAuthority("ROLE_HR"));
                    } else {
                        return java.util.stream.Stream.of(new SimpleGrantedAuthority(name));
                    }
                })
                .collect(Collectors.toList());

        Long employeeId = user.getEmployee() != null ? user.getEmployee().getId() : null;

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                employeeId
        );
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
