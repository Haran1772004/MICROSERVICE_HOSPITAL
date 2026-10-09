package com.hospital.auth.client;

import com.hospital.auth.dto.CreateDoctorProfileRequest;
import com.hospital.auth.dto.CreatePatientProfileRequest;
import com.hospital.auth.dto.RemoteError;
import com.hospital.common.exception.ApiException;
import com.hospital.common.exception.ServiceUnavailableException;
import com.hospital.common.security.InternalApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Set;

/**
 * Calls the internal endpoints of hospital-service. Every call sends the
 * {@code X-Internal-Secret} header and has a short timeout (3 seconds by default).
 *
 * <p>Error rules:
 * <ul>
 *   <li>If hospital-service answers 400, 404, 409 or 422, the same status and message are
 *       passed on to the caller (for example "phone already exists" stays a 409).</li>
 *   <li>If hospital-service is down, too slow, or answers anything else (401, 403, 5xx),
 *       a {@link ServiceUnavailableException} (503) is thrown.</li>
 * </ul>
 */
@Component
public class HospitalServiceClient {

    private static final Logger log = LoggerFactory.getLogger(HospitalServiceClient.class);

    /** Statuses from hospital-service that are business errors the user should see. */
    private static final Set<Integer> PASS_THROUGH_STATUSES = Set.of(400, 404, 409, 422);

    private static final String UNAVAILABLE_MESSAGE =
            "Hospital service is unavailable. Please try again later.";

    private final RestClient restClient;

    /**
     * Creates the client.
     *
     * @param baseUrl   value of {@code HOSPITAL_SERVICE_URL}
     * @param timeoutMs connect and read timeout in milliseconds
     * @param secret    value of {@code INTERNAL_API_SECRET}
     */
    public HospitalServiceClient(@Value("${services.hospital-url}") String baseUrl,
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
     * Creates the patient profile (and the address, if given) in hospital-service.
     *
     * @param request the patient data
     * @throws ApiException                if hospital-service rejected the data
     *                                     (400, 404, 409 or 422)
     * @throws ServiceUnavailableException if hospital-service cannot be used
     */
    public void createPatient(CreatePatientProfileRequest request) {
        post("/internal/patients", request);
    }

    /**
     * Creates the doctor profile (status PENDING) in hospital-service.
     *
     * @param request the doctor data
     * @throws ApiException                if hospital-service rejected the data
     *                                     (400, 404, 409 or 422)
     * @throws ServiceUnavailableException if hospital-service cannot be used
     */
    public void createDoctor(CreateDoctorProfileRequest request) {
        post("/internal/doctors", request);
    }

    /**
     * Removes the profile of a user in hospital-service. Used to undo a registration.
     *
     * @param userId id of the user in auth-service
     * @throws ServiceUnavailableException if the call fails
     */
    public void deleteProfileByUser(int userId) {
        try {
            restClient.delete()
                    .uri("/internal/profiles/by-user/{userId}", userId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new ServiceUnavailableException(UNAVAILABLE_MESSAGE);
        }
    }

    private void post(String path, Object body) {
        try {
            restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw translate(exception);
        } catch (RestClientException exception) {
            log.error("Hospital service call to {} failed: {}", path,
                    exception.getClass().getSimpleName());
            throw new ServiceUnavailableException(UNAVAILABLE_MESSAGE);
        }
    }

    private ApiException translate(RestClientResponseException exception) {
        int status = exception.getStatusCode().value();
        if (PASS_THROUGH_STATUSES.contains(status)) {
            return new ApiException(status, extractMessage(exception));
        }
        log.error("Hospital service answered unexpected status {}", status);
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
        return "Hospital service rejected the request.";
    }
}
