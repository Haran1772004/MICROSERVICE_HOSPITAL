package com.hospital.appointment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.appointment.dto.MedicalRecordRequest;
import com.hospital.appointment.dto.MedicalRecordResponse;
import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.MedicalRecord;
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.appointment.repository.MedicalRecordRepository;
import com.hospital.common.enums.AppointmentStatus;
import com.hospital.common.exception.ConflictException;
import com.hospital.common.exception.ResourceNotFoundException;

/**
 * Business logic for medical records.
 */
@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * Creates the service.
     *
     * @param medicalRecordRepository repository for records
     * @param appointmentRepository   repository for appointments
     */
    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                               AppointmentRepository appointmentRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Creates a medical record for a scheduled appointment.
     *
     * @param request the record request
     * @return the created record
     */
    @Transactional
    public MedicalRecordResponse createMedicalRecord(MedicalRecordRequest request) {
        Appointment appointment = appointmentRepository.findByAppointmentId(request.appointmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Appointment not found."));
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ConflictException("Only scheduled appointments can have a medical record.");
        }
        if (medicalRecordRepository.existsByAppointmentId(request.appointmentId())) {
            throw new ConflictException("A medical record already exists for this appointment.");
        }

        MedicalRecord record = new MedicalRecord(
                request.appointmentId(),
                request.diagnosis().trim(),
                request.treatmentNotes().trim(),
                request.recordDate());
        MedicalRecord saved = medicalRecordRepository.save(record);

        appointment.setStatus(AppointmentStatus.FINISHED);
        appointmentRepository.save(appointment);

        return MedicalRecordResponse.from(
                saved, appointment.getPatientId(), appointment.getDoctorId());
    }

    /**
     * Reads all records.
     *
     * @return all records
     */
    public List<MedicalRecordResponse> getAllMedicalRecords() {
        return medicalRecordRepository.findAllByOrderByRecordDateDescRecordIdDesc().stream()
                .map(record -> {
                    Appointment appointment = appointmentRepository
                            .findByAppointmentId(record.getAppointmentId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("Appointment not found."));
                    return MedicalRecordResponse.from(
                            record, appointment.getPatientId(),
                            appointment.getDoctorId());
                })
                .toList();
    }

    /**
     * Reads one record.
     *
     * @param recordId id of the record
     * @return the record
     */
    public MedicalRecordResponse getMedicalRecord(int recordId) {
        MedicalRecord record = medicalRecordRepository.findByRecordId(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found."));
        Appointment appointment = appointmentRepository
                .findByAppointmentId(record.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found."));
        return MedicalRecordResponse.from(
                record, appointment.getPatientId(), appointment.getDoctorId());
    }

    /**
     * Reads the record for one appointment.
     *
     * @param appointmentId id of the appointment
     * @return the record
     */
    public MedicalRecordResponse getMedicalRecordByAppointment(int appointmentId) {
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found."));
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Appointment not found."));
        return MedicalRecordResponse.from(
                record, appointment.getPatientId(), appointment.getDoctorId());
    }

    /**
     * Reads all medical records for a patient.
     *
     * @param patientId id of the patient
     * @return matching records
     */
    public List<MedicalRecordResponse> getMedicalRecordsByPatient(int patientId) {
        List<Integer> appointmentIds = appointmentRepository
                .findByPatientIdOrderByAppointmentDateAscAppointmentTimeAsc(patientId)
                .stream()
                .map(Appointment::getAppointmentId)
                .toList();
        return medicalRecordRepository.findByAppointmentIdIn(appointmentIds).stream()
                .map(record -> {
                    Appointment appointment = appointmentRepository
                            .findByAppointmentId(record.getAppointmentId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("Appointment not found."));
                    return MedicalRecordResponse.from(
                            record, appointment.getPatientId(),
                            appointment.getDoctorId());
                })
                .toList();
    }

    /**
     * Reads all medical records for a doctor.
     *
     * @param doctorId id of the doctor
     * @return matching records
     */
    public List<MedicalRecordResponse> getMedicalRecordsByDoctor(int doctorId) {
        List<Integer> appointmentIds = appointmentRepository
                .findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(doctorId)
                .stream()
                .map(Appointment::getAppointmentId)
                .toList();
        return medicalRecordRepository.findByAppointmentIdIn(appointmentIds).stream()
                .map(record -> {
                    Appointment appointment = appointmentRepository
                            .findByAppointmentId(record.getAppointmentId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("Appointment not found."));
                    return MedicalRecordResponse.from(
                            record, appointment.getPatientId(),
                            appointment.getDoctorId());
                })
                .toList();
    }
}
