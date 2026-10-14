package com.hospital.notification.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hospital.notification.client.HospitalServiceClient;
import com.hospital.notification.dto.AppointmentEvent;
import com.hospital.notification.dto.DoctorInfo;
import com.hospital.notification.dto.PatientInfo;
import com.hospital.notification.entity.Reminder;
import com.hospital.notification.entity.ReminderStatus;
import com.hospital.notification.repository.ReminderRepository;

/**
 * Creates, cancels and sends reminders.
 */
@Service
public class ReminderService {

    private static final Logger LOG = LoggerFactory.getLogger(ReminderService.class);

    private final ReminderRepository reminderRepository;
    private final HospitalServiceClient hospitalServiceClient;
    private final long minutesBefore;
    private final ZoneId zone;

    public ReminderService(ReminderRepository reminderRepository,
                           HospitalServiceClient hospitalServiceClient,
                           @Value("${reminder.minutes-before}") long minutesBefore,
                           @Value("${app.time-zone}") String timeZone) {
        this.reminderRepository = reminderRepository;
        this.hospitalServiceClient = hospitalServiceClient;
        this.minutesBefore = minutesBefore;
        this.zone = ZoneId.of(timeZone);
    }

    /**
     * Saves a reminder for a newly booked appointment.
     */
    public void createReminder(AppointmentEvent event) {
        LocalDateTime appointmentTime = LocalDateTime.of(
                LocalDate.parse(event.appointmentDate()), LocalTime.parse(event.appointmentTime()));

        if (!appointmentTime.isAfter(LocalDateTime.now(zone))) {
            LOG.info("Appointment {} is already in the past. No reminder saved.", event.appointmentId());
            return;
        }
        if (reminderRepository.findByAppointmentId(event.appointmentId()).isPresent()) {
            LOG.info("Reminder for appointment {} already exists.", event.appointmentId());
            return;
        }

        PatientInfo patient = hospitalServiceClient.getPatient(event.patientId());
        DoctorInfo doctor = hospitalServiceClient.getDoctor(event.doctorId());

        Reminder reminder = new Reminder();
        reminder.setAppointmentId(event.appointmentId());
        reminder.setPatientId(event.patientId());
        reminder.setPatientName(patient != null ? patient.name() : "Patient #" + event.patientId());
        reminder.setDoctorName(doctor != null ? doctor.name() : "Doctor #" + event.doctorId());
        reminder.setAppointmentDateTime(appointmentTime);
        reminder.setRemindAt(appointmentTime.minusMinutes(minutesBefore));
        reminderRepository.save(reminder);

        LOG.info("Reminder saved for appointment {}. It will be sent at {}.",
                event.appointmentId(), reminder.getRemindAt());
    }

    /**
     * Cancels the reminder of a cancelled appointment.
     */
    public void cancelReminder(AppointmentEvent event) {
        reminderRepository.findByAppointmentId(event.appointmentId()).ifPresent(reminder -> {
            if (reminder.getStatus() == ReminderStatus.PENDING) {
                reminder.setStatus(ReminderStatus.CANCELLED);
                reminderRepository.save(reminder);
                LOG.info("Reminder for appointment {} cancelled.", event.appointmentId());
            }
        });
    }

    /**
     * Sends all reminders whose time has come.
     */
    public void sendDueReminders() {
        List<Reminder> due = reminderRepository.findByStatusAndRemindAtLessThanEqual(
                ReminderStatus.PENDING, LocalDateTime.now(zone));

        for (Reminder reminder : due) {
            // For this project the "notification" is a log message.
            LOG.info("REMINDER >>> Hello {}, you have an appointment with {} on {} at {}.",
                    reminder.getPatientName(), reminder.getDoctorName(),
                    reminder.getAppointmentDateTime().toLocalDate(),
                    reminder.getAppointmentDateTime().toLocalTime());
            reminder.setStatus(ReminderStatus.SENT);
            reminderRepository.save(reminder);
        }
    }

    public List<Reminder> getAllReminders() {
        return reminderRepository.findAllByOrderByAppointmentDateTimeAsc();
    }
}
