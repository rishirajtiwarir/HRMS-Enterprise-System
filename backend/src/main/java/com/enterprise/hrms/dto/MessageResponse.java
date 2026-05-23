package com.enterprise.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for sending standard string messages back to client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String message;
}
