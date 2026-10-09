package com.hospital.hospitalservice.security;

import com.hospital.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.Instant;

/**
 * Writes an error body in the same shape as {@link ErrorResponse} from inside
 * security filters, where the normal exception handler is not available.
 */
public final class SecurityErrorWriter {

    private SecurityErrorWriter() {
    }

    /**
     * Writes a JSON error to the response.
     *
     * @param response the response to write to
     * @param status   the HTTP status code
     * @param message  the message for the caller (a fixed text, not user input)
     * @throws IOException if writing fails
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
