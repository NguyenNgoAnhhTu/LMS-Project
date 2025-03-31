package com.example.assessment.model;

import com.example.course.Course;
import com.example.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_final_score")
public class CourseFinalScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private Float score;

    private LocalDateTime date;

    public void updateFinalScore() {
        // Aggregate logic for final score
        // Omitted for brevity
    }

    // Getters and Setters
    // Omitted for brevity
}
