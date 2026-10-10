# Shared Rules for All 3 Services

Give this whole file to every AI. Do not change these rules without telling the others.
The parent `pom.xml`, the `common` library and the empty service skeletons are already made.
**Do not edit `pom.xml` of the parent or anything inside `common`.** Work only inside your own service folder.

## 1. Who owns what

| Service | Port | Database | Tables | Java package |
|---|---|---|---|---|
| auth-service | 8081 | `auth_db` | `users` | `com.hospital.auth` |
| hospital-service | 8082 | `hospital_db` | `departments`, `doctors`, `patients`, `patient_addresses` | `com.hospital.hospitalservice` |
| appointment-service | 8083 | `appointment_db` | `appointments`, `medical_records`, `prescriptions` | `com.hospital.appointment` |

Use PostgreSQL. Use the old MySQL schema as a guide, but change it to PostgreSQL syntax.

## 2. No links between databases
- No foreign key across services. These columns are plain numbers:
  - `patients.user_id`, `doctors.user_id` (user is in auth_db)
  - `appointments.patient_id`, `appointments.doctor_id` (patient and doctor are in hospital_db)
- Inside one service, normal foreign keys are fine.
- In `appointment-service`, entities store `patientId` and `doctorId` as plain `int`. Do not use `@ManyToOne` to Patient or Doctor.
- Responses from appointment-service return only IDs. Do not return `patientName`, `doctorName`, `departmentName`.
- Keep the "one active appointment per doctor/patient per time slot" rule (the `active_slot` trick). In PostgreSQL use a partial unique index: `UNIQUE (doctor_id, appointment_date, appointment_time) WHERE status = 'SCHEDULED'` (same for patient).

## 3. Shared code (from `common`)
Use these. Do not write your own copies.
- Enums: `Role`, `Gender`, `AccountStatus`, `AppointmentStatus`, `AddressType`
- Exceptions: `BadRequestException` (400), `UnauthorizedException` (401), `ForbiddenException` (403), `ResourceNotFoundException` (404), `ConflictException` (409), `ServiceUnavailableException` (503)
- `ErrorResponse` (error body), `JwtUtil` and `JwtUser` (token), `InternalApi` (header name)

Each service writes its own `GlobalExceptionHandler` that turns any `ApiException` into `ErrorResponse` using `getStatus()`.
Also handle validation errors (400), wrong login (401) and access denied (403) in the same `ErrorResponse` format.

## 4. Security (same in all 3 services)
- Login happens only in auth-service. It returns a JWT made by `JwtUtil`.
- Every service checks the JWT itself with `JwtUtil` (same `JWT_SECRET`). Make a `JwtUtil` bean in a config class.
- Roles: `ADMIN`, `RECEPTIONIST`, `DOCTOR`, `PATIENT`. Use `@PreAuthorize("hasRole('...')")`.
- Missing or bad token returns 401. Wrong role returns 403.
- Endpoints under `/internal/**` are NOT for users. They need the header `X-Internal-Secret` equal to `INTERNAL_API_SECRET`. A user JWT must not open them.
- Passwords are hashed with BCrypt (auth-service only).

## 5. Settings (environment variables, no passwords in code)
`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, `INTERNAL_API_SECRET`,
`HOSPITAL_SERVICE_URL`, `AUTH_SERVICE_URL`, `REDIS_HOST`, `REDIS_PORT`.
Defaults are already in each `application.yml`. Keep `ddl-auto: none`. Each service ships its own `schema.sql` (or Flyway scripts) for its tables.

## 6. Public endpoints (keep the same URLs as the Postman collection)

**auth-service**
- `POST /auth/login`
- `POST /auth/register/patient` — creates user (ACTIVE), then calls hospital-service to create the patient and address. If that call fails, delete the user and return 503.
- `POST /auth/register/doctor` — creates user (PENDING), then calls hospital-service to create the doctor (PENDING). If that fails, delete the user.
- `GET /users`, `GET /users/{username}`, `GET /users/pending/{role}`, `PUT /users/{username}/status` (ADMIN)
- `PATCH /admin/users/{userId}/deactivate` (ADMIN) — replaces the old hard delete.

**hospital-service**
- Departments: `GET/POST /departments`, `GET/PUT /departments/{id}`, `PATCH /departments/{id}/activate|deactivate`
- Doctors: `GET/POST /doctors`, `GET/PUT /doctors/{id}`, `PATCH /doctors/{id}/activate|deactivate`
- Admin: `GET /admin/doctors/pending`, `POST /admin/doctors/{doctorId}/approve`, `POST /admin/doctors/{doctorId}/reject`
  - Approve: set doctor ACTIVE here, then call auth-service to set the user ACTIVE. Reject: same with REJECTED.
- Patients: `GET /patients`, `GET /patients/{id}`, `PUT /patients/{id}`, `PATCH /patients/{id}/activate|deactivate`, `GET /patients/status/{status}`, `GET/PUT /patients/me`
- Addresses: `GET/POST /patient-addresses/me`, `GET /patient-addresses/me/default`, `PUT /patient-addresses/{id}`, `GET /patient-addresses/patient/{patientId}`
- Cache (Caffeine, local): departments, doctors list, doctor by id. Clear the cache on update, activate, deactivate.

**appointment-service**
- `POST /appointments` — RECEPTIONIST sends `patientId`. PATIENT does not send it; the service finds it from the JWT `userId` (call hospital-service).
- `GET /appointments`, `GET /appointments/today`, `GET /appointments/{id}`
- `GET /appointments/patient/{patientId}`, `GET /appointments/doctor/{doctorId}`, `GET /appointments/doctor/{doctorId}/today`, `GET /appointments/doctor/{doctorId}/available`
- `PATCH /appointments/{id}/cancel`
- `POST /medical-records`, `GET /medical-records`, `GET /medical-records/{id}`, `GET /medical-records/appointment/{id}`, `GET /medical-records/patient/{patientId}`, `GET /medical-records/doctor/{doctorId}`
- `POST /prescriptions`, `GET /prescriptions/record/{recordId}`

## 7. Removed endpoints (do NOT build)
- `GET /patients/me/profile`
- `GET /patients/me/appointments`
- `GET /patients/me/medical-records`
- `GET /patients/me/prescriptions`
- `DELETE /admin/users/{username}` (hard delete)

The client uses these instead: `/patients/me` to get `patientId`, then `/appointments/patient/{id}`, `/medical-records/patient/{id}`, `/prescriptions/record/{recordId}`, `/patient-addresses/me`.

## 8. Internal endpoints (service to service)
All need the `X-Internal-Secret` header.

**hospital-service provides**
- `GET /internal/patients/{patientId}` → `{patientId, userId, name, status}`
- `GET /internal/patients/by-user/{userId}` → same body
- `GET /internal/doctors/{doctorId}` → `{doctorId, userId, name, departmentId, status}`
- `POST /internal/patients` → body: `userId, name, dob, gender, phone, email, state, district, pincode, addressType`
- `POST /internal/doctors` → body: `userId, name, specialization, phone, email, departmentId`
- `DELETE /internal/profiles/by-user/{userId}` → removes the profile if registration must be undone

**auth-service provides**
- `PATCH /internal/users/{userId}/status` → body: `{status}`
- `DELETE /internal/users/{userId}` → used only to undo a failed registration

**Who calls whom**
- auth-service → hospital-service (registration)
- hospital-service → auth-service (approve / reject doctor)
- appointment-service → hospital-service (check patient and doctor)
- Use `RestClient`. If the other service is down or answers an error, return `ServiceUnavailableException` (503). Set a short timeout (3 seconds).

**Booking rule (appointment-service):** before saving, call hospital-service. Patient and doctor must exist and be `ACTIVE`. Then check the time slot is free.
**Medical record rule:** the appointment must exist in this service and be `SCHEDULED`. After saving the record, set the appointment to `FINISHED`. One record per appointment.

## 9. Code style (all services)
- Java 17, Oracle Java code conventions: 4 spaces, one class per file, `CamelCase` classes, `camelCase` methods, `UPPER_CASE` constants, lines under 100 characters.
- Use normal getters like `getName()` (the old project used `takeName()` — do not copy that).
- Constructor injection only. No field `@Autowired`.
- Layers: `controller` → `service` → `repository`. Controllers never call repositories.
- Use DTO classes for requests and responses. Do not return entities directly.
- Validate input (`@Valid`, `@NotBlank`, etc.).
- JavaDoc on every public class and public method (what it does, `@param`, `@return`, `@throws`).
- No `System.out.println`. Use SLF4J logging.
- Never log passwords or tokens.

## 10. Not now
Docker, Kafka and notification-service come later. Do not add them yet. But keep the app starting from environment variables so Docker is easy later.
