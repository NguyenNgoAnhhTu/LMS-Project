package com.example.department;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    Optional<Department> findById(Long id);
    Page<Department> findAll(Pageable pageable);
    Page<Department> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Department> findByName(String name);
}
