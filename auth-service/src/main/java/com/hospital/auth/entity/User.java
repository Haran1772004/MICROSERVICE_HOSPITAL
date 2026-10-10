package com.hospital.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.hospital.common.enums.AccountStatus;
import com.hospital.common.enums.Role;

/**
 * A login account. It belongs to a patient, a doctor, a receptionist or an admin. The patient or
 * doctor profile lives in hospital-service and points back to {@link #getUserId()}. This entity is
 * never returned from the API; use {@code UserResponse} instead.
 */
@Entity
@Table(name = "users")
public class User {

    private static final int USERNAME_COLUMN_LENGTH = 20;
    private static final int PASSWORD_COLUMN_LENGTH = 60;
    private static final int ROLE_COLUMN_LENGTH = 12;
    private static final int STATUS_COLUMN_LENGTH = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int userId;

    @Column(name = "username", nullable = false, unique = true, length = USERNAME_COLUMN_LENGTH)
    private String username;

    @Column(name = "password", nullable = false, length = PASSWORD_COLUMN_LENGTH)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = ROLE_COLUMN_LENGTH)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = STATUS_COLUMN_LENGTH)
    private AccountStatus status;

    /** Creates an empty user. Required by JPA. */
    protected User() { }

    /**
      * Creates a new user that is not saved yet.
      *
      * @param username unique login name
      * @param passwordHash BCrypt hash of the password (never the plain password)
      * @param role role of the user
      * @param status account status
      */
    public User(String username, String passwordHash, Role role, AccountStatus status) {
        this.username = username;
        this.password = passwordHash;
        this.role = role;
        this.status = status;
    }

    /**
      * Returns the id of the user.
      *
      * @return the user id (0 before the user is saved)
      */
    public int getUserId() {
        return userId;
    }

    /**
      * Returns the login name.
      *
      * @return the username
      */
    public String getUsername() {
        return username;
    }

    /**
      * Returns the BCrypt hash of the password.
      *
      * @return the password hash
      */
    public String getPassword() {
        return password;
    }

    /**
      * Returns the role.
      *
      * @return the role
      */
    public Role getRole() {
        return role;
    }

    /**
      * Returns the account status.
      *
      * @return the status
      */
    public AccountStatus getStatus() {
        return status;
    }

    /**
      * Changes the account status.
      *
      * @param status the new status
      */
    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}
