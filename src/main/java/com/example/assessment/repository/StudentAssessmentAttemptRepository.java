package com.example.assessment.repository;


import com.example.assessment.model.ProgrammingLanguage;
import com.example.assessment.model.StudentAssessmentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentAssessmentAttemptRepository extends JpaRepository<StudentAssessmentAttempt, Integer> {

    // Create or update a student assessment attempt
    StudentAssessmentAttempt save(StudentAssessmentAttempt attempt);

    // Find an attempt by its ID
    Optional<StudentAssessmentAttempt> findById(Long id);

    // Find all attempts for a specific assessment
    List<StudentAssessmentAttempt> findByAssessmentId(Long assessmentId);

    // Find all attempts by a specific user
    List<StudentAssessmentAttempt> findByUserId(Long userId);

    // Delete an attempt by its ID
    void deleteById(Long id);

    // Find all attempts
    List<StudentAssessmentAttempt> findAll();
}
