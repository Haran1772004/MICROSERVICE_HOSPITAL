package com.hospital.auth;

import com.hospital.auth.entity.User;
import com.hospital.auth.repository.UserRepository;
import com.hospital.common.enums.AccountStatus;
import com.hospital.common.enums.Role;
import com.hospital.common.security.JwtUtil;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the auth-service against a real PostgreSQL database and a fake hospital-service.
 *
 * <p>The database is read from DB_URL, DB_USERNAME and DB_PASSWORD (default
 * {@code jdbc:postgresql://localhost:5432/auth_db}, user {@code postgres}). If the database
 * cannot be reached, the tests are skipped. The tests use random usernames that start with
 * {@code tst_} and delete them at the end.
 */
@SpringBootTest(properties = {
        "jwt.secret=test-jwt-secret-that-is-longer-than-32-characters",
        "internal.api-secret=test-internal-secret",
        "spring.datasource.password=${DB_PASSWORD:postgres}"
})
class AuthServiceIntegrationTest {

    private static final String DB_URL =
            System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/auth_db");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USERNAME", "postgres");
    private static final String DB_PASSWORD =
            System.getenv().getOrDefault("DB_PASSWORD", "postgres");
    private static final String SECRET = "test-internal-secret";
    private static final String PASSWORD = "Passw0rd";

    private static FakeHospitalService hospital;

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private Filter springSecurityFilterChain;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;

    @BeforeAll
    static void startFakeHospital() throws Exception {
        boolean reachable;
        try (Connection ignored = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            reachable = true;
        } catch (Exception exception) {
            reachable = false;
        }
        Assumptions.assumeTrue(reachable, "PostgreSQL is not reachable: tests skipped");
        hospital = new FakeHospitalService();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("services.hospital-url", () -> "http://localhost:" + hospital.port());
    }

    @AfterAll
    static void cleanUp() throws Exception {
        if (hospital == null) {
            return;
        }
        hospital.stop();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             java.sql.Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM users WHERE username LIKE 'tst\\_%'");
        }
    }

    @BeforeEach
    void setUp() {
        hospital.reset();
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    // ---------- helpers ----------

    private String newName() {
        return "tst_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private User saveUser(Role role, AccountStatus status) {
        return userRepository.save(
                new User(newName(), passwordEncoder.encode(PASSWORD), role, status));
    }

    private String bearer(Role role) {
        return "Bearer " + jwtUtil.generateToken("someone", 1, role);
    }

    private String loginBody(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    private String patientBody(String username) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\","
                + "\"name\":\"Test Patient\",\"dob\":\"1995-05-15\",\"gender\":\"MALE\","
                + "\"phone\":\"9876543210\",\"email\":\"p@example.com\","
                + "\"state\":\"Tamil Nadu\",\"district\":\"Tenkasi\",\"pincode\":\"627751\","
                + "\"addressType\":\"HOME\"}";
    }

    private String doctorBody(String username) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\","
                + "\"name\":\"Dr Test\",\"specialization\":\"Cardiology\","
                + "\"phone\":\"9876500001\",\"email\":\"d@example.com\",\"departmentId\":1}";
    }

    // ---------- login ----------

    @Test
    @DisplayName("Login: active user gets a token that JwtUtil can read")
    void loginSuccess() throws Exception {
        User user = saveUser(Role.PATIENT, AccountStatus.ACTIVE);

        String json = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(user.getUsername(), PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andReturn().getResponse().getContentAsString();

        String token = json.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
        assertTrue(jwtUtil.parse(token).isPresent());
        assertEquals(user.getUserId(), jwtUtil.parse(token).get().userId());
        assertEquals(Role.PATIENT, jwtUtil.parse(token).get().role());
    }

    @Test
    @DisplayName("Login: wrong password gives 401")
    void loginWrongPassword() throws Exception {
        User user = saveUser(Role.PATIENT, AccountStatus.ACTIVE);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(user.getUsername(), "WrongPass1")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    @Test
    @DisplayName("Login: PENDING, REJECTED and INACTIVE accounts get 401")
    void loginBlockedStatuses() throws Exception {
        for (AccountStatus blocked : new AccountStatus[] {
                AccountStatus.PENDING, AccountStatus.REJECTED, AccountStatus.INACTIVE}) {
            User user = saveUser(Role.DOCTOR, blocked);
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginBody(user.getUsername(), PASSWORD)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ---------- registration ----------

    @Test
    @DisplayName("Register patient: ACTIVE user, hospital-service called with secret and userId")
    void registerPatient() throws Exception {
        String username = newName();

        mockMvc.perform(post("/auth/register/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientBody(username)))
                .andExpect(status().isCreated());

        User user = userRepository.findByUsername(username).orElseThrow();
        assertEquals(AccountStatus.ACTIVE, user.getStatus());
        assertEquals(Role.PATIENT, user.getRole());
        assertFalse(user.getPassword().equals(PASSWORD));

        assertEquals(1, hospital.calls().size());
        FakeHospitalService.Call call = hospital.calls().get(0);
        assertEquals("POST", call.method());
        assertEquals("/internal/patients", call.path());
        assertEquals(SECRET, call.secret());
        assertTrue(call.body().contains("\"userId\":" + user.getUserId()));
        assertTrue(call.body().contains("\"dob\":\"1995-05-15\""));
        assertTrue(call.body().contains("\"addressType\":\"HOME\""));
    }

    @Test
    @DisplayName("Register patient: hospital-service 409 is passed on and the user is removed")
    void registerPatientConflictFromHospital() throws Exception {
        hospital.answerWith(409, "{\"status\":409,\"message\":"
                + "\"A patient with this phone number already exists.\"}");
        String username = newName();

        mockMvc.perform(post("/auth/register/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientBody(username)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("A patient with this phone number already exists."));

        assertTrue(userRepository.findByUsername(username).isEmpty());
        assertEquals(1, hospital.calls().size());
    }

    @Test
    @DisplayName("Register patient: hospital-service 500 gives 503, user removed, profile undone")
    void registerPatientHospitalFails() throws Exception {
        hospital.answerWith(500, "");
        String username = newName();

        mockMvc.perform(post("/auth/register/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientBody(username)))
                .andExpect(status().isServiceUnavailable());

        assertTrue(userRepository.findByUsername(username).isEmpty());
        boolean undoCalled = hospital.calls().stream()
                .anyMatch(call -> call.method().equals("DELETE")
                        && call.path().startsWith("/internal/profiles/by-user/"));
        assertTrue(undoCalled);
    }

    @Test
    @DisplayName("Register doctor: PENDING user, hospital-service called")
    void registerDoctor() throws Exception {
        String username = newName();

        mockMvc.perform(post("/auth/register/doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(doctorBody(username)))
                .andExpect(status().isCreated());

        User user = userRepository.findByUsername(username).orElseThrow();
        assertEquals(AccountStatus.PENDING, user.getStatus());
        assertEquals(Role.DOCTOR, user.getRole());
        assertEquals("/internal/doctors", hospital.calls().get(0).path());
    }

    @Test
    @DisplayName("Register: blank username gives 400 and no call to hospital-service")
    void registerValidation() throws Exception {
        mockMvc.perform(post("/auth/register/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientBody("")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username is required."));

        assertTrue(hospital.calls().isEmpty());
    }

    @Test
    @DisplayName("Register: taken username gives 409 and no call to hospital-service")
    void registerDuplicateUsername() throws Exception {
        User existing = saveUser(Role.PATIENT, AccountStatus.ACTIVE);

        mockMvc.perform(post("/auth/register/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientBody(existing.getUsername())))
                .andExpect(status().isConflict());

        assertTrue(hospital.calls().isEmpty());
    }

    // ---------- internal endpoints ----------

    @Test
    @DisplayName("Internal: no secret, or only a user JWT, gives 401")
    void internalNeedsSecret() throws Exception {
        User user = saveUser(Role.DOCTOR, AccountStatus.PENDING);

        mockMvc.perform(patch("/internal/users/" + user.getUserId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/internal/users/" + user.getUserId() + "/status")
                        .header("Authorization", bearer(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isUnauthorized());

        assertEquals(AccountStatus.PENDING,
                userRepository.findById(user.getUserId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Internal: with the secret, status can be changed and a user deleted")
    void internalWithSecret() throws Exception {
        User user = saveUser(Role.DOCTOR, AccountStatus.PENDING);

        mockMvc.perform(patch("/internal/users/" + user.getUserId() + "/status")
                        .header("X-Internal-Secret", SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk());
        assertEquals(AccountStatus.ACTIVE,
                userRepository.findById(user.getUserId()).orElseThrow().getStatus());

        mockMvc.perform(delete("/internal/users/" + user.getUserId())
                        .header("X-Internal-Secret", SECRET))
                .andExpect(status().isOk());
        assertTrue(userRepository.findById(user.getUserId()).isEmpty());

        mockMvc.perform(delete("/internal/users/" + user.getUserId())
                        .header("X-Internal-Secret", SECRET))
                .andExpect(status().isNotFound());
    }

    // ---------- admin and user management ----------

    @Test
    @DisplayName("Admin deactivate: ADMIN only, and the user can no longer log in")
    void adminDeactivate() throws Exception {
        User user = saveUser(Role.PATIENT, AccountStatus.ACTIVE);

        mockMvc.perform(patch("/admin/users/" + user.getUserId() + "/deactivate")
                        .header("Authorization", bearer(Role.PATIENT)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/admin/users/" + user.getUserId() + "/deactivate")
                        .header("Authorization", bearer(Role.ADMIN)))
                .andExpect(status().isOk());
        assertEquals(AccountStatus.INACTIVE,
                userRepository.findById(user.getUserId()).orElseThrow().getStatus());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(user.getUsername(), PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

        @Test
    @DisplayName("Users: admin creates a receptionist and changes status")
    void adminManagesUsers() throws Exception {
        String username = newName();
        String admin = bearer(Role.ADMIN);

        mockMvc.perform(post("/users")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username
                                + "\",\"password\":\"secret1\",\"role\":\"receptionist\"}"))
                .andExpect(status().isOk());
        assertEquals(Role.RECEPTIONIST,
                userRepository.findByUsername(username).orElseThrow().getRole());

        mockMvc.perform(put("/users/" + username + "/status?status=INACTIVE")
                        .header("Authorization", admin))
                .andExpect(status().isOk());
        mockMvc.perform(get("/users/" + username).header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        mockMvc.perform(put("/users/" + username + "/status?status=NOPE")
                        .header("Authorization", admin))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Users: admin cannot create a PATIENT or DOCTOR login")
    void adminCannotCreatePatientOrDoctor() throws Exception {
        for (String role : new String[] {"patient", "doctor"}) {
            mockMvc.perform(post("/users")
                            .header("Authorization", bearer(Role.ADMIN))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"" + newName()
                                    + "\",\"password\":\"secret1\",\"role\":\""
                                    + role + "\"}"))
                    .andExpect(status().isBadRequest());
        }
    }
}

