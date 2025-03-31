package com.example.user;

import com.example.role.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find all users with pagination
    Page<User> findAll(Pageable pageable);


    // Find users by username (case insensitive search)
    Optional<User> findByUsername(String username);
    // Find users by email (case insensitive search)
    Optional<User> findByEmail(String email);

    // Find users by their username
    Page<User> findByUsernameContainingIgnoreCase(String lastName, Pageable pageable);

    boolean existsByRolesContains(Role role);

    User getUserById(Long id);

    // Find users by their last name
    Page<User> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);

    // Find users by first name
    Page<User> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);

    // Find users by locked status
    Page<User> findByIsLocked(Boolean isLocked, Pageable pageable);

    // Find users by two-factor authentication enabled status
    Page<User> findByIs2faEnabled(Boolean is2faEnabled, Pageable pageable);

    // find all user by role
    List<User> findByRoles(List<Role> roles);

    boolean existsByUsername(String username);
}
