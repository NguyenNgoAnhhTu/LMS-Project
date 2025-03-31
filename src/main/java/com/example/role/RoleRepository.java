package com.example.role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Page<Role> findAll(Pageable pageable);
    Page<Role> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
}