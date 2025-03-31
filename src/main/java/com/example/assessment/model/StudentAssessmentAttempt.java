package com.example.assessment.model;

import com.example.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_assessment_attempt")
public class StudentAssessmentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Min(0)
    private int duration;

    @Min(0)
    private int scoreQuiz;

    @Min(0)
    private int scoreAss;

    private String note;

    private boolean isSubmitted;

    private boolean isProctored;

    @Lob
    private String proctoringData;

    @Column(nullable = false, updatable = false)
    private LocalDateTime attemptDate = LocalDateTime.now();

    // Getters and Setters
    // Omitted for brevity
}
