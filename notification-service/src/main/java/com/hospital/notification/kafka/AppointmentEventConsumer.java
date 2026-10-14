package com.hospital.notification.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

import com.hospital.notification.dto.AppointmentEvent;
import com.hospital.notification.service.ReminderService;

/**
 * Listens to appointment events from Kafka.
 */
@Component
public class AppointmentEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentEventConsumer.class);

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final ReminderService reminderService;

    public AppointmentEventConsumer(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @KafkaListener(topics = "appointment-events", groupId = "notification-group")
    public void onMessage(String message) {
        LOG.info("Received event: {}", message);
        try {
            AppointmentEvent event = jsonMapper.readValue(message, AppointmentEvent.class);
            if ("BOOKED".equals(event.type())) {
                reminderService.createReminder(event);
            } else if ("CANCELLED".equals(event.type())) {
                reminderService.cancelReminder(event);
            } else {
                LOG.warn("Unknown event type: {}", event.type());
            }
        } catch (RuntimeException e) {
            LOG.error("Could not process event: {}", e.getMessage());
        }
    }
}
