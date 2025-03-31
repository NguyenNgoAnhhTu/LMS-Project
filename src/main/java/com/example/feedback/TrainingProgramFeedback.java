package com.example.feedback;

import java.time.LocalDateTime;

import com.example.training_programing.TrainingProgram;
import com.example.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class TrainingProgramFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "training_program_id")
    private TrainingProgram trainingProgram;

    @Column(nullable = false)
    private Integer relevance;

    @Column(nullable = false)
    private Integer organization;

    @Column(nullable = false)
    private Integer learningOutcomes;

    @Column(nullable = false)
    private Integer resources;

    @Column(nullable = false)
    private Integer support;

    @Transient
    public Double averageRating() {
        return (relevance + organization + learningOutcomes + resources + support) / 5.0;
    }

    @Column(length = 500)
    private String comments;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters
}

