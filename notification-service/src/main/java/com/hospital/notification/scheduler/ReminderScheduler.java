package com.hospital.notification.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hospital.notification.service.ReminderService;

/**
 * Timer that checks for due reminders.
 */
@Component
public class ReminderScheduler {

    private final ReminderService reminderService;

    public ReminderScheduler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(fixedDelayString = "${reminder.check-interval-ms}")
    public void run() {
        reminderService.sendDueReminders();
    }
}
