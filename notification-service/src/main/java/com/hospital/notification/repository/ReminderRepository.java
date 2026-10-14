package com.hospital.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hospital.notification.entity.Reminder;
import com.hospital.notification.entity.ReminderStatus;

/**
 * Data access for reminders.
 */
@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Integer> {

    Optional<Reminder> findByAppointmentId(int appointmentId);

    List<Reminder> findByStatusAndRemindAtLessThanEqual(ReminderStatus status, LocalDateTime time);

    List<Reminder> findAllByOrderByAppointmentDateTimeAsc();
}
