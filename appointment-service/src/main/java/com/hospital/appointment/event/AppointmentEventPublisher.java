package com.hospital.appointment.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

import com.hospital.appointment.dto.AppointmentEvent;
import com.hospital.appointment.dto.AppointmentResponse;

/**
 * Sends appointment events to the Kafka topic "appointment-events".
 */
@Component
public class AppointmentEventPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentEventPublisher.class);
    private static final String TOPIC = "appointment-events";

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final KafkaTemplate<String, String> kafkaTemplate;

    public AppointmentEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Sends one event. If Kafka is down, booking still works and only an error is logged.
     *
     * @param appointment the appointment
     * @param type        BOOKED or CANCELLED
     */
    public void publish(AppointmentResponse appointment, String type) {
        try {
            AppointmentEvent event = new AppointmentEvent(
                    appointment.appointmentId(),
                    appointment.patientId(),
                    appointment.doctorId(),
                    appointment.appointmentDate().toString(),
                    appointment.appointmentTime().toString(),
                    type);
            String json = jsonMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, String.valueOf(appointment.appointmentId()), json);
            LOG.info("Sent {} event for appointment {}", type, appointment.appointmentId());
        } catch (RuntimeException e) {
            LOG.error("Could not send appointment event: {}", e.getMessage());
        }
    }
}