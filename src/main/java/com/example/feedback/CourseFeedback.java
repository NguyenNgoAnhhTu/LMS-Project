package com.example.feedback;

import java.time.LocalDateTime;
import java.util.Set;

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
public class CourseFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false)
    private Integer courseMaterial;

    @Column(nullable = false)
    private Integer clarityOfExplanation;

    @Column(nullable = false)
    private Integer courseStructure;

    @Column(nullable = false)
    private Integer practicalApplications;

    @Column(nullable = false)
    private Integer supportMaterials;

    @ManyToMany
    @JoinTable(
            name = "helpful_rate",
            joinColumns = @JoinColumn(name = "feedback_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> helpfulRate;

    @Transient
    public Double averageRating() {
        return (courseMaterial + clarityOfExplanation + courseStructure + practicalApplications + supportMaterials) / 5.0;
    }

    @Column(length = 500)
    private String courseComment;

    @Column(length = 500)
    private String materialComment;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters
}

