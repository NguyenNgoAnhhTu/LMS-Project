package com.example.assessment.service;


import com.example.assessment.model.InvitedCandidate;
import com.example.assessment.repository.InvitedCandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InvitedCandidateService  {

    private final InvitedCandidateRepository repository;

    @Autowired
    public InvitedCandidateService(InvitedCandidateRepository repository) {
        this.repository = repository;
    }

    public InvitedCandidate save(InvitedCandidate invitedCandidate) {
        return repository.save(invitedCandidate);
    }

    public Optional<InvitedCandidate> findById(Long id) {
        return repository.findById(id);
    }

    public List<InvitedCandidate> findByAssessmentId(Long assessmentId) {
        return repository.findByAssessmentId(assessmentId);
    }

    public Optional<InvitedCandidate> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public List<InvitedCandidate> findAll() {
        return repository.findAll();
    }
}

