package com.example.training_programing;

import com.example.course.Course;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
public class TrainingProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String programName;

    @Column(nullable = false, unique = true)
    private String programCode;

    @Column(nullable = true)
    private String description;

    @ManyToMany
    @JoinTable(
            name = "program_courses",
            joinColumns = @JoinColumn(name = "training_program_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses;

    @Override
    public String toString() {
        return programName;
    }
}

