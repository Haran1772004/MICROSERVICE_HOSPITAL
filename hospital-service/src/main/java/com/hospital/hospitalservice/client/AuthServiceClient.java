package com.hospital.hospitalservice.client;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.hospital.common.enums.AccountStatus;
import com.hospital.common.exception.ApiException;
import com.hospital.common.exception.ServiceUnavailableException;
import com.hospital.common.security.InternalApi;
import com.hospital.hospitalservice.dto.RemoteError;
import com.hospital.hospitalservice.dto.StatusBody;

/**
 * Calls the internal endpoints of auth-service. Every call sends the
 * {@code X-Internal-Secret} header and has a short timeout (3 seconds by default).
 *
 * <p>Error rules:
 * <ul>
 *   <li>If auth-service answers 400, 404, 409 or 422, the same status and message are
 *       passed on to the caller.</li>
 *   <li>If auth-service is down, too slow, or answers anything else (401, 403, 5xx),
 *       a {@link ServiceUnavailableException} (503) is thrown.</li>
 * </ul>
 */
@Component
public class AuthServiceClient {

    private static final Logger LOG = LoggerFactory.getLogger(AuthServiceClient.class);

    /** Statuses from auth-service that are business errors the user should see. */
    private static final Set<Integer> PASS_THROUGH_STATUSES = Set.of(400, 404, 409, 422);

    private static final String UNAVAILABLE_MESSAGE =
            "Auth service is unavailable. Please try again later.";

    private final RestClient restClient;

    /**
     * Creates the client.
     *
     * @param baseUrl   value of {@code AUTH_SERVICE_URL}
     * @param timeoutMs connect and read timeout in milliseconds
     * @param secret    value of {@code INTERNAL_API_SECRET}
     */
    public AuthServiceClient(@Value("${services.auth-url}") String baseUrl,
                             @Value("${services.timeout-ms}") long timeoutMs,
                             @Value("${internal.api-secret}") String secret) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(InternalApi.SECRET_HEADER, secret)
                .build();
    }

    /**
     * Changes the status of a user in auth-service.
     *
     * @param userId id of the user in auth-service
     * @param status the new status
     * @throws ApiException                if auth-service rejected the call (400, 404, 409
     *                                     or 422)
     * @throws ServiceUnavailableException if auth-service cannot be used
     */
    public void updateUserStatus(int userId, AccountStatus status) {
        try {
            restClient.patch()
                    .uri("/internal/users/{userId}/status", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new StatusBody(status.name()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw translate(exception);
        } catch (RestClientException exception) {
            LOG.error("Auth service call failed: {}", exception.getClass().getSimpleName());
            throw new ServiceUnavailableException(UNAVAILABLE_MESSAGE);
        }
    }

    private ApiException translate(RestClientResponseException exception) {
        int status = exception.getStatusCode().value();
        if (PASS_THROUGH_STATUSES.contains(status)) {
            return new ApiException(status, extractMessage(exception));
        }
        LOG.error("Auth service answered unexpected status {}", status);
        return new ServiceUnavailableException(UNAVAILABLE_MESSAGE);
    }

    private String extractMessage(RestClientResponseException exception) {
        try {
            RemoteError error = exception.getResponseBodyAs(RemoteError.class);
            if (error != null && error.message() != null && !error.message().isBlank()) {
                return error.message();
            }
        } catch (RuntimeException ignored) {
            // The body was not in the expected format. Use the default text below.
        }
        return "Auth service rejected the request.";
    }
}
