package com.example.feedback;

import java.time.LocalDateTime;

import com.example.course.Course;
import com.example.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class InstructorFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private User instructor;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false)
    private Integer courseKnowledge;

    @Column(nullable = false)
    private Integer communicationSkills;

    @Column(nullable = false)
    private Integer approachability;

    @Column(nullable = false)
    private Integer engagement;

    @Column(nullable = false)
    private Integer professionalism;

    @Transient
    public Double averageRating() {
        return (courseKnowledge + communicationSkills + approachability + engagement + professionalism) / 5.0;
    }

    @Column(length = 500)
    private String comments;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters
}

