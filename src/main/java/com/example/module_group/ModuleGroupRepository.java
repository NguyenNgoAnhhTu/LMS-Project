package com.example.module_group;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModuleGroupRepository extends JpaRepository<ModuleGroup, Long> {
    // Additional query methods can be defined here if needed
    Optional<ModuleGroup> findByName(String name);
    Page<ModuleGroup> findAll(Pageable pageable);
    Page<ModuleGroup> findByNameContainingIgnoreCase(String name, Pageable pageable);
}

