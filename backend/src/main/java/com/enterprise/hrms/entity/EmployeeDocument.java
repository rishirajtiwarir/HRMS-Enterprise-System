package com.enterprise.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotBlank(message = "Document name is required")
    @Column(name = "document_name", nullable = false)
    private String documentName;

    @NotBlank(message = "Document type is required")
    @Column(name = "document_type", nullable = false)
    private String documentType; // ID, CONTRACT, RESUME, SICK_LEAVE_DOC, etc.

    @NotBlank(message = "File content is required")
    @Column(name = "file_content", nullable = false, columnDefinition = "LONGTEXT")
    private String fileContent; // Base64 encoded string representing the file

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }
}
