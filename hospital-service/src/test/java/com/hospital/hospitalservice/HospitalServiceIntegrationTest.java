package com.hospital.hospitalservice;

import com.hospital.common.enums.AccountStatus;
import com.hospital.common.enums.AddressType;
import com.hospital.common.enums.Gender;
import com.hospital.common.enums.Role;
import com.hospital.common.security.JwtUtil;
import com.hospital.hospitalservice.entity.Department;
import com.hospital.hospitalservice.entity.Doctor;
import com.hospital.hospitalservice.entity.Patient;
import com.hospital.hospitalservice.entity.PatientAddress;
import com.hospital.hospitalservice.repository.DepartmentRepository;
import com.hospital.hospitalservice.repository.DoctorRepository;
import com.hospital.hospitalservice.repository.PatientAddressRepository;
import com.hospital.hospitalservice.repository.PatientRepository;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the hospital-service against a real PostgreSQL database and a fake auth-service.
 *
 * <p>The database is read from DB_URL, DB_USERNAME and DB_PASSWORD (default
 * {@code jdbc:postgresql://localhost:5433/hospital_db}, user {@code postgres}). If the
 * database cannot be reached, the tests are skipped. The tests use random names and emails
 * that start with {@code tst_} and delete them at the end.
 */
@SpringBootTest(properties = {
        "jwt.secret=test-jwt-secret-that-is-longer-than-32-characters",
        "internal.api-secret=test-internal-secret",
        "spring.datasource.password=${DB_PASSWORD:postgres}",
        "spring.cache.type=simple"
})
class HospitalServiceIntegrationTest {

    private static final String DB_URL = System.getenv()
            .getOrDefault("DB_URL", "jdbc:postgresql://localhost:5433/hospital_db");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USERNAME", "postgres");
    private static final String DB_PASSWORD =
            System.getenv().getOrDefault("DB_PASSWORD", "postgres");
    private static final String SECRET = "test-internal-secret";

    private static FakeAuthService auth;

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private Filter springSecurityFilterChain;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private PatientAddressRepository addressRepository;
    @Autowired
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;

    @BeforeAll
    static void startFakeAuth() throws Exception {
        boolean reachable;
        try (Connection ignored = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            reachable = true;
        } catch (Exception exception) {
            reachable = false;
        }
        Assumptions.assumeTrue(reachable, "PostgreSQL is not reachable: tests skipped");
        auth = new FakeAuthService();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("services.auth-url", () -> "http://localhost:" + auth.port());
    }

    @AfterAll
    static void cleanUp() throws Exception {
        if (auth == null) {
            return;
        }
        auth.stop();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM patient_addresses WHERE patient_id IN "
                    + "(SELECT patient_id FROM patients WHERE email LIKE 'tst\\_%')");
            statement.executeUpdate("DELETE FROM patients WHERE email LIKE 'tst\\_%'");
            statement.executeUpdate("DELETE FROM doctors WHERE email LIKE 'tst\\_%'");
            statement.executeUpdate("DELETE FROM departments WHERE name LIKE 'tst\\_%'");
        }
    }

    @BeforeEach
    void setUp() {
        auth.reset();
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    // ---------- helpers ----------

    private String newText() {
        return "tst_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String newEmail() {
        return newText() + "@example.com";
    }

    private String newPhone() {
        return "9" + String.format("%09d", ThreadLocalRandom.current().nextLong(1_000_000_000L));
    }

    private int newUserId() {
        return ThreadLocalRandom.current().nextInt(1_000_000, 2_000_000_000);
    }

    private String bearer(Role role) {
        return bearer(role, 1);
    }

    private String bearer(Role role, int userId) {
        return "Bearer " + jwtUtil.generateToken("someone", userId, role);
    }

    private Department saveDepartment(AccountStatus status) {
        return departmentRepository.save(new Department(newText(), "test", status));
    }

    private Doctor saveDoctor(Integer userId, Department department, AccountStatus status) {
        return doctorRepository.save(new Doctor(userId, "Dr Test", "Cardiology", newPhone(),
                newEmail(), department, status));
    }

    private Patient savePatient(Integer userId, AccountStatus status) {
        return patientRepository.save(new Patient(userId, "Test Patient",
                LocalDate.of(1995, 5, 15), Gender.MALE, newPhone(), newEmail(), status));
    }

    private String patientBody(String phone, String email) {
        return "{\"name\":\"Test Patient\",\"dob\":\"1995-05-15\",\"gender\":\"MALE\","
                + "\"phone\":\"" + phone + "\",\"email\":\"" + email + "\"}";
    }

    private String doctorBody(String phone, String email, int departmentId) {
        return "{\"name\":\"Dr Test\",\"specialization\":\"Cardiology\",\"phone\":\""
                + phone + "\",\"email\":\"" + email + "\",\"departmentId\":"
                + departmentId + "}";
    }

    private String addressBody(String type) {
        return "{\"state\":\"Tamil Nadu\",\"district\":\"Tenkasi\",\"pincode\":\"627751\","
                + "\"addressType\":\"" + type + "\"}";
    }

    // ---------- internal endpoints ----------

    @Test
    @DisplayName("Internal: no secret, or only a user JWT, gives 401")
    void internalNeedsSecret() throws Exception {
        mockMvc.perform(get("/internal/patients/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/internal/patients/1")
                        .header("Authorization", bearer(Role.ADMIN)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Internal: create patient gives ACTIVE patient and the first address")
    void internalCreatePatient() throws Exception {
        int userId = newUserId();
        String phone = newPhone();
        String email = newEmail();
        String body = "{\"userId\":" + userId + ",\"name\":\"Test Patient\","
                + "\"dob\":\"1995-05-15\",\"gender\":\"MALE\",\"phone\":\"" + phone
                + "\",\"email\":\"" + email + "\",\"state\":\"Tamil Nadu\","
                + "\"district\":\"Tenkasi\",\"pincode\":\"627751\",\"addressType\":\"HOME\"}";

        mockMvc.perform(post("/internal/patients")
                        .header("X-Internal-Secret", SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        Patient patient = patientRepository.findByUserId(userId).orElseThrow();
        assertEquals(AccountStatus.ACTIVE, patient.getStatus());
        assertEquals(1, addressRepository.findByPatientIdOrderByAddressType(
                patient.getPatientId()).size());

        // the same phone again gives 409 with the old message
        mockMvc.perform(post("/internal/patients")
                        .header("X-Internal-Secret", SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("\"userId\":" + userId,
                                "\"userId\":" + newUserId())
                                .replace(email, newEmail())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Patient with phone already exists: " + phone));
    }

    @Test
    @DisplayName("Internal: create doctor gives PENDING doctor, and the right error codes")
    void internalCreateDoctor() throws Exception {
        Department active = saveDepartment(AccountStatus.ACTIVE);
        Department inactive = saveDepartment(AccountStatus.INACTIVE);
        int userId = newUserId();
        String email = newEmail();
        String body = "{\"userId\":" + userId + ",\"name\":\"Dr Test\","
                + "\"specialization\":\"Cardiology\",\"phone\":\"" + newPhone()
                + "\",\"email\":\"" + email + "\",\"departmentId\":" + active.getDepartmentId()
                + "}";

        mockMvc.perform(post("/internal/doctors")
                        .header("X-Internal-Secret", SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
        assertEquals(AccountStatus.PENDING,
                doctorRepository.findByUserId(userId).orElseThrow().getStatus());

        // duplicate email: 409
        mockMvc.perform(post("/internal/doctors")
                        .header("X-Internal-Secret", SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("\"userId\":" + userId,
                                "\"userId\":" + newUserId())))
                .andExpect(status().isConflict());

        // department that does not exist: 404
        mockMvc.perform(post("/internal/doctors")
                        .header("X-Internal-Secret", SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + newUserId() + ",\"name\":\"Dr Test\","
                                + "\"specialization\":\"Cardiology\",\"phone\":\"" + newPhone()
                                + "\",\"email\":\"" + newEmail() + "\",\"departmentId\":999999}"))
                .andExpect(status().isNotFound());

        // inactive department: 400
        mockMvc.perform(post("/internal/doctors")
                        .header("X-Internal-Secret", SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + newUserId() + ",\"name\":\"Dr Test\","
                                + "\"specialization\":\"Cardiology\",\"phone\":\"" + newPhone()
                                + "\",\"email\":\"" + newEmail() + "\",\"departmentId\":"
                                + inactive.getDepartmentId() + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Internal: delete profile by user works, and also when there is no profile")
    void internalDeleteProfile() throws Exception {
        int userId = newUserId();
        Patient patient = savePatient(userId, AccountStatus.ACTIVE);
        addressRepository.save(new PatientAddress(patient.getPatientId(), "Tamil Nadu",
                "Tenkasi", "627751", AddressType.HOME));

        mockMvc.perform(delete("/internal/profiles/by-user/" + userId)
                        .header("X-Internal-Secret", SECRET))
                .andExpect(status().isOk());
        assertTrue(patientRepository.findByUserId(userId).isEmpty());
        assertTrue(addressRepository.findByPatientIdOrderByAddressType(
                patient.getPatientId()).isEmpty());

        mockMvc.perform(delete("/internal/profiles/by-user/" + userId)
                        .header("X-Internal-Secret", SECRET))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Internal: lookups return short data, or 404")
    void internalLookups() throws Exception {
        int userId = newUserId();
        Patient patient = savePatient(userId, AccountStatus.ACTIVE);
        Doctor doctor = saveDoctor(newUserId(), saveDepartment(AccountStatus.ACTIVE),
                AccountStatus.ACTIVE);

        mockMvc.perform(get("/internal/patients/" + patient.getPatientId())
                        .header("X-Internal-Secret", SECRET))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(patient.getPatientId()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        mockMvc.perform(get("/internal/patients/by-user/" + userId)
                        .header("X-Internal-Secret", SECRET))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId));
        mockMvc.perform(get("/internal/doctors/" + doctor.getDoctorId())
                        .header("X-Internal-Secret", SECRET))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(doctor.getDoctorId()));
        mockMvc.perform(get("/internal/doctors/999999")
                        .header("X-Internal-Secret", SECRET))
                .andExpect(status().isNotFound());
    }

    // ---------- departments ----------

    @Test
    @DisplayName("Departments: admin manages them, others only read, the cache is cleared")
    void departments() throws Exception {
        String name = newText();
        String json = mockMvc.perform(post("/departments")
                        .header("Authorization", bearer(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"description\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();
        int id = Integer.parseInt(json.replaceAll(".*\"departmentId\":(\\d+).*", "$1"));

        // same name again: 409
        mockMvc.perform(post("/departments")
                        .header("Authorization", bearer(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isConflict());

        // a patient can read but not create
        mockMvc.perform(get("/departments/" + id).header("Authorization", bearer(Role.PATIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name));
        mockMvc.perform(post("/departments")
                        .header("Authorization", bearer(Role.PATIENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + newText() + "\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/departments")).andExpect(status().isUnauthorized());

        // update: the cached answer must change too
        String newName = newText();
        mockMvc.perform(put("/departments/" + id)
                        .header("Authorization", bearer(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + newName + "\",\"description\":\"changed\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/departments/" + id).header("Authorization", bearer(Role.PATIENT)))
                .andExpect(jsonPath("$.name").value(newName));

        // deactivate with PATCH, activate with PUT (old style)
        mockMvc.perform(patch("/departments/" + id + "/deactivate")
                        .header("Authorization", bearer(Role.ADMIN)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/departments/" + id).header("Authorization", bearer(Role.PATIENT)))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
        mockMvc.perform(put("/departments/" + id + "/activate")
                        .header("Authorization", bearer(Role.ADMIN)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/departments/" + id).header("Authorization", bearer(Role.PATIENT)))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(get("/departments/999999")
                        .header("Authorization", bearer(Role.ADMIN)))
                .andExpect(status().isNotFound());
    }

    // ---------- doctors ----------

    @Test
    @DisplayName("Doctors: admin creates, updates, deactivates and activates them")
    void doctors() throws Exception {
        Department department = saveDepartment(AccountStatus.ACTIVE);
        String admin = bearer(Role.ADMIN);

        String json = mockMvc.perform(post("/doctors")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(doctorBody(newPhone(), newEmail(),
                                department.getDepartmentId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.department.departmentId")
                        .value(department.getDepartmentId()))
                .andReturn().getResponse().getContentAsString();
        int id = Integer.parseInt(json.replaceAll(".*\"doctorId\":(\\d+).*", "$1"));

        // the old nested department form also works
        mockMvc.perform(put("/doctors/" + id)
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dr Changed\",\"specialization\":\"Neurology\","
                                + "\"phone\":\"" + newPhone() + "\",\"email\":\"" + newEmail()
                                + "\",\"department\":{\"departmentId\":"
                                + department.getDepartmentId() + "}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dr Changed"));

        mockMvc.perform(patch("/doctors/" + id + "/deactivate").header("Authorization", admin))
                .andExpect(status().isOk());
        mockMvc.perform(get("/doctors/" + id).header("Authorization", bearer(Role.PATIENT)))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
        mockMvc.perform(patch("/doctors/" + id + "/activate").header("Authorization", admin))
                .andExpect(status().isOk());
        mockMvc.perform(get("/doctors/" + id).header("Authorization", bearer(Role.PATIENT)))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // a patient cannot create a doctor
        mockMvc.perform(post("/doctors")
                        .header("Authorization", bearer(Role.PATIENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(doctorBody(newPhone(), newEmail(),
                                department.getDepartmentId())))
                .andExpect(status().isForbidden());

        // bad phone: 400 with the old message
        mockMvc.perform(post("/doctors")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(doctorBody("abc", newEmail(), department.getDepartmentId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid phone number format"));
    }

    @Test
    @DisplayName("Admin: approve and reject call auth-service and change the doctor")
    void approveAndReject() throws Exception {
        Department department = saveDepartment(AccountStatus.ACTIVE);
        int approveUser = newUserId();
        int rejectUser = newUserId();
        Doctor toApprove = saveDoctor(approveUser, department, AccountStatus.PENDING);
        Doctor toReject = saveDoctor(rejectUser, department, AccountStatus.PENDING);
        String admin = bearer(Role.ADMIN);

        mockMvc.perform(get("/admin/doctors/pending").header("Authorization", admin))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/doctors/pending")
                        .header("Authorization", bearer(Role.PATIENT)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/admin/doctors/" + toApprove.getDoctorId() + "/approve")
                        .header("Authorization", admin))
                .andExpect(status().isOk());
        assertEquals(AccountStatus.ACTIVE,
                doctorRepository.findById(toApprove.getDoctorId()).orElseThrow().getStatus());
        FakeAuthService.Call call = auth.calls().get(0);
        assertEquals("PATCH", call.method());
        assertEquals("/internal/users/" + approveUser + "/status", call.path());
        assertEquals(SECRET, call.secret());
        assertTrue(call.body().contains("ACTIVE"));

        mockMvc.perform(post("/admin/doctors/" + toReject.getDoctorId() + "/reject")
                        .header("Authorization", admin))
                .andExpect(status().isOk());
        assertEquals(AccountStatus.REJECTED,
                doctorRepository.findById(toReject.getDoctorId()).orElseThrow().getStatus());

        // a doctor that is not PENDING anymore cannot be approved again
        mockMvc.perform(post("/admin/doctors/" + toApprove.getDoctorId() + "/approve")
                        .header("Authorization", admin))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Admin: if auth-service fails, the doctor stays PENDING")
    void approveWhenAuthFails() throws Exception {
        Department department = saveDepartment(AccountStatus.ACTIVE);
        Doctor doctorOne = saveDoctor(newUserId(), department, AccountStatus.PENDING);
        Doctor doctorTwo = saveDoctor(newUserId(), department, AccountStatus.PENDING);
        String admin = bearer(Role.ADMIN);

        auth.answerWith(500, "");
        mockMvc.perform(post("/admin/doctors/" + doctorOne.getDoctorId() + "/approve")
                        .header("Authorization", admin))
                .andExpect(status().isServiceUnavailable());
        assertEquals(AccountStatus.PENDING,
                doctorRepository.findById(doctorOne.getDoctorId()).orElseThrow().getStatus());

        auth.answerWith(404, "{\"status\":404,\"message\":\"User not found.\"}");
        mockMvc.perform(post("/admin/doctors/" + doctorTwo.getDoctorId() + "/approve")
                        .header("Authorization", admin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found."));
        assertEquals(AccountStatus.PENDING,
                doctorRepository.findById(doctorTwo.getDoctorId()).orElseThrow().getStatus());
    }

    // ---------- patients ----------

    @Test
    @DisplayName("Patients: staff manage them, validation and duplicates work")
    void patientsByStaff() throws Exception {
        String receptionist = bearer(Role.RECEPTIONIST);
        String phone = newPhone();
        String email = newEmail();

        String json = mockMvc.perform(post("/patients")
                        .header("Authorization", receptionist)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientBody(phone, email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        int id = Integer.parseInt(json.replaceAll(".*\"patientId\":(\\d+).*", "$1"));

        mockMvc.perform(post("/patients")
                        .header("Authorization", receptionist)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientBody(phone, newEmail())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Patient with phone already exists: " + phone));
        mockMvc.perform(post("/patients")
                        .header("Authorization", receptionist)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientBody("12", newEmail())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid phone number format"));

        mockMvc.perform(patch("/patients/" + id + "/activate")
                        .header("Authorization", receptionist))
                .andExpect(status().isOk());
        mockMvc.perform(get("/patients/" + id).header("Authorization", receptionist))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        mockMvc.perform(get("/patients/status/active")
                        .header("Authorization", receptionist))
                .andExpect(status().isOk());
        mockMvc.perform(get("/patients/status/nope")
                        .header("Authorization", receptionist))
                .andExpect(status().isBadRequest());

        // a doctor may read a patient, a patient may not list patients
        mockMvc.perform(get("/patients/" + id).header("Authorization", bearer(Role.DOCTOR)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/patients").header("Authorization", bearer(Role.PATIENT)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Patients: a patient sees and changes only the own profile")
    void patientOwnProfile() throws Exception {
        int userId = newUserId();
        Patient own = savePatient(userId, AccountStatus.ACTIVE);
        Patient other = savePatient(newUserId(), AccountStatus.ACTIVE);
        String token = bearer(Role.PATIENT, userId);

        mockMvc.perform(get("/patients/me").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(own.getPatientId()));
        mockMvc.perform(get("/patients/" + own.getPatientId()).header("Authorization", token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/patients/" + other.getPatientId()).header("Authorization", token))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/patients/me")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Changed Name\",\"dob\":\"1990-01-01\","
                                + "\"gender\":\"FEMALE\",\"phone\":\"" + newPhone()
                                + "\",\"email\":\"" + newEmail() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Changed Name"));

        // a user without a patient profile gets 403
        mockMvc.perform(get("/patients/me").header("Authorization", bearer(Role.PATIENT,
                        newUserId())))
                .andExpect(status().isForbidden());
    }

    // ---------- addresses ----------

    @Test
    @DisplayName("Addresses: a patient manages only own addresses, staff manage any")
    void addresses() throws Exception {
        int userId = newUserId();
        Patient own = savePatient(userId, AccountStatus.ACTIVE);
        Patient other = savePatient(newUserId(), AccountStatus.ACTIVE);
        PatientAddress othersAddress = addressRepository.save(new PatientAddress(
                other.getPatientId(), "Kerala", "Kochi", "682001", AddressType.HOME));
        String token = bearer(Role.PATIENT, userId);

        String json = mockMvc.perform(post("/patient-addresses/me")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressBody("HOME")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(own.getPatientId()))
                .andReturn().getResponse().getContentAsString();
        int addressId = Integer.parseInt(json.replaceAll(".*\"addressId\":(\\d+).*", "$1"));

        // the same type again gives 409
        mockMvc.perform(post("/patient-addresses/me")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressBody("HOME")))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/patient-addresses/" + addressId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"state\":\"Tamil Nadu\",\"district\":\"Madurai\","
                                + "\"pincode\":\"625001\",\"addressType\":\"HOME\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.district").value("Madurai"));
        mockMvc.perform(get("/patient-addresses/me/default").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressId").value(addressId));

        // another patient's address is not allowed
        mockMvc.perform(put("/patient-addresses/" + othersAddress.getAddressId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressBody("HOME")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/patient-addresses/patient/" + other.getPatientId())
                        .header("Authorization", token))
                .andExpect(status().isForbidden());

        // staff can add an address to any patient, but must send the patient id
        mockMvc.perform(post("/patient-addresses")
                        .header("Authorization", bearer(Role.RECEPTIONIST))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patientId\":" + other.getPatientId()
                                + ",\"state\":\"Kerala\",\"district\":\"Kochi\","
                                + "\"pincode\":\"682001\",\"addressType\":\"WORK\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/patient-addresses")
                        .header("Authorization", bearer(Role.RECEPTIONIST))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressBody("WORK")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/patient-addresses/" + addressId).header("Authorization", token))
                .andExpect(status().isOk());
        assertTrue(addressRepository.findById(addressId).isEmpty());
    }
}
