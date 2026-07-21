package com.substring.chat.chat_app_backend.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response returned by all REST endpoints.
 *
 * <p>Provides a consistent JSON shape for error responses across the entire API,
 * making it predictable for frontend consumers to handle errors.</p>
 *
 * <p>Example response:</p>
 * <pre>{@code
 * {
 *   "timestamp": "2026-07-10T09:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Room not found: lobby",
 *   "path": "/api/v1/rooms/lobby",
 *   "fieldErrors": null
 * }
 * }</pre>
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    /** Field-level validation errors (field name → error message). Only present for 400 validation failures. */
    private Map<String, String> fieldErrors;
}
