package com.example.assessment.service;


import com.example.assessment.model.StudentAssessmentAttempt;
import com.example.assessment.repository.StudentAssessmentAttemptRepository;
import com.example.assessment.repository.StudentAssessmentAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentAssessmentAttemptService {

    @Autowired
    private StudentAssessmentAttemptRepository repository;

    public StudentAssessmentAttempt save(StudentAssessmentAttempt attempt) {
        return repository.save(attempt);
    }

    public Optional<StudentAssessmentAttempt> findById(Long id) {
        return repository.findById(id);
    }

    public List<StudentAssessmentAttempt> findByAssessmentId(Long assessmentId) {
        return repository.findByAssessmentId(assessmentId);
    }

    public List<StudentAssessmentAttempt> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public List<StudentAssessmentAttempt> findAll() {
        return repository.findAll();
    }
}

