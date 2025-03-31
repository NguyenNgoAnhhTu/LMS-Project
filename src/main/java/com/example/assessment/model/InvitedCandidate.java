package com.example.assessment.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "invited_candidate")
public class InvitedCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, updatable = false)
    private LocalDateTime invitationDate = LocalDateTime.now();

    private LocalDateTime expirationDate;

    // Utility method to set expiration date
    public void setExpirationDate(int days) {
        this.expirationDate = this.invitationDate.plusDays(days);
    }

    // Getters and Setters
    // Omitted for brevity
}
