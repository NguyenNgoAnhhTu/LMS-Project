package com.example.course.section;

import com.example.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findAllByCourse(Course course);
}
