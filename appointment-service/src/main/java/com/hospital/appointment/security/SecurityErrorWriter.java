package com.hospital.appointment.security;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;

/**
 * Writes a JSON error body from inside security filters.
 */
public final class SecurityErrorWriter {

    private SecurityErrorWriter() {
    }

    /**
     * Writes an error response.
     *
     * @param response the response to write to
     * @param status   the HTTP status code
     * @param message  the human-readable message
     * @throws IOException if the response cannot be written
     */
    public static void write(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String safeMessage = message.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("{\"status\":" + status
                + ",\"message\":\"" + safeMessage
                + "\",\"timestamp\":\"" + Instant.now() + "\"}");
    }
}
