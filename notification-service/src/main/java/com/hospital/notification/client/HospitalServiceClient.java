package com.hospital.notification.client;

import java.net.http.HttpClient;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.hospital.common.security.InternalApi;
import com.hospital.notification.dto.DoctorInfo;
import com.hospital.notification.dto.PatientInfo;

/**
 * Reads patient and doctor names from hospital-service.
 * If hospital-service is down, it returns null and the caller uses a fallback name.
 */
@Component
public class HospitalServiceClient {

    private static final Logger LOG = LoggerFactory.getLogger(HospitalServiceClient.class);

    private final RestClient restClient;

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

    public PatientInfo getPatient(int patientId) {
        try {
            return restClient.get().uri("/internal/patients/{id}", patientId)
                    .retrieve().body(PatientInfo.class);
        } catch (RuntimeException e) {
            LOG.warn("Could not read patient {}: {}", patientId, e.getMessage());
            return null;
        }
    }

    public DoctorInfo getDoctor(int doctorId) {
        try {
            return restClient.get().uri("/internal/doctors/{id}", doctorId)
                    .retrieve().body(DoctorInfo.class);
        } catch (RuntimeException e) {
            LOG.warn("Could not read doctor {}: {}", doctorId, e.getMessage());
            return null;
        }
    }
}
