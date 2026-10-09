package com.hospital.appointment.client;

import com.hospital.appointment.dto.DoctorInfo;
import com.hospital.appointment.dto.PatientInfo;
import com.hospital.appointment.dto.RemoteError;
import com.hospital.common.exception.ApiException;
import com.hospital.common.exception.ServiceUnavailableException;
import com.hospital.common.security.InternalApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Set;

/**
 * Calls the hospital-service internal endpoints.
 */
@Component
public class HospitalServiceClient {

    private static final Logger log = LoggerFactory.getLogger(HospitalServiceClient.class);

    private static final Set<Integer> PASS_THROUGH_STATUSES = Set.of(400, 404, 409, 422);

    private static final String UNAVAILABLE_MESSAGE =
            "Hospital service is unavailable. Please try again later.";

    private final RestClient restClient;

    /**
     * Creates the client.
     *
     * @param baseUrl   value of {@code HOSPITAL_SERVICE_URL}
     * @param timeoutMs request timeout in milliseconds
     * @param secret    internal secret used by both services
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
     * Reads a patient record by hospital id.
     *
     * @param patientId id of the patient
     * @return the short patient data
     */
    public PatientInfo getPatient(int patientId) {
        return get("/internal/patients/{patientId}", PatientInfo.class, patientId);
    }

    /**
     * Reads a patient record by auth user id.
     *
     * @param userId id of the auth-service user
     * @return the short patient data
     */
    public PatientInfo getPatientByUser(int userId) {
        return get("/internal/patients/by-user/{userId}", PatientInfo.class, userId);
    }

    /**
     * Reads a doctor record by hospital id.
     *
     * @param doctorId id of the doctor
     * @return the short doctor data
     */
    public DoctorInfo getDoctor(int doctorId) {
        return get("/internal/doctors/{doctorId}", DoctorInfo.class, doctorId);
    }

    private <T> T get(String path, Class<T> type, Object... values) {
        try {
            return restClient.get()
                    .uri(path, values)
                    .retrieve()
                    .body(type);
        } catch (RestClientResponseException exception) {
            throw translate(exception);
        } catch (RestClientException exception) {
            log.error("Hospital service call failed for {}: {}", path,
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
            // The body was not in the expected format.
        }
        return "Hospital service rejected the request.";
    }
}
