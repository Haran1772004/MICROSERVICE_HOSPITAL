package com.hospital.notification.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.notification.entity.Reminder;
import com.hospital.notification.service.ReminderService;

/**
 * Simple endpoint to see all reminders (useful for the demo).
 */
@RestController
@RequestMapping("/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping
    public List<Reminder> getAll() {
        return reminderService.getAllReminders();
    }
}
