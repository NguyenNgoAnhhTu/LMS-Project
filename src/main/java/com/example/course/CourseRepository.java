package com.example.course;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findAll(Pageable pageable);
    Page<Course> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Course> findByName(String name);
    Course findById(long id);
}
