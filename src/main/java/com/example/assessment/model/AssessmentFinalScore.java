package com.example.assessment.model;

import com.example.user.User;
import jakarta.persistence.*;

@Entity
@Table(name = "assessment_final_score")
public class AssessmentFinalScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    private float finalScoreAss;

    private float finalScoreQuiz;

    private float finalScore;

    public void updateScore(float weightAss, float weightQuiz) {
        // Update logic, using Hibernate queries as needed
        // Omitted for brevity
    }

    // Getters and Setters
    // Omitted for brevity
}

