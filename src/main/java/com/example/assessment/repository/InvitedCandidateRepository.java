package com.example.assessment.repository;

import com.example.assessment.model.InvitedCandidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvitedCandidateRepository extends JpaRepository<InvitedCandidate, Long> {

    @Query("SELECT ic FROM InvitedCandidate ic WHERE ic.email LIKE %:searchQuery%")
    Page<InvitedCandidate> searchByEmail(@Param("searchQuery") String searchQuery, Pageable pageable);

    // Find all invited candidates for a specific assessment
    @Query("SELECT ic FROM InvitedCandidate ic WHERE ic.assessment.id = :assessmentId")
    Page<InvitedCandidate> findByAssessmentId(@Param("assessmentId") Long assessmentId, Pageable pageable);

    // Find all invited candidates (paged)
    Page<InvitedCandidate> findAll(Pageable pageable);

    // Find an invited candidate by email
    Optional<InvitedCandidate> findByEmail(String email);

    // Find all invited candidates for a specific assessment (non-paged)
    List<InvitedCandidate> findByAssessmentId(Long assessmentId);

    // Save or update an invited candidate (provided by JpaRepository)
    InvitedCandidate save(InvitedCandidate invitedCandidate);
}
