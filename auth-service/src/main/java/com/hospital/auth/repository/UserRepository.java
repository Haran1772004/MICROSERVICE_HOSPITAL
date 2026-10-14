package com.hospital.auth.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hospital.auth.entity.User;
import com.hospital.common.enums.AccountStatus;
import com.hospital.common.enums.Role;

/** Database access for {@link User}. */
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
      * Finds a user by login name.
      *
      * @param username the login name
      * @return the user, or empty if there is none
      */
    Optional<User> findByUsername(String username);

    /**
      * Checks if a login name is already used.
      *
      * @param username the login name
      * @return true if a user with this name exists
      */
    boolean existsByUsername(String username);

    /**
      * Finds all users with a role and a status.
      *
      * @param role the role
      * @param status the status
      * @return the matching users
      */
    List<User> findByRoleAndStatus(Role role, AccountStatus status);
}
