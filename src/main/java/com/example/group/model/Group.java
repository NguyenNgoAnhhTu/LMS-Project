package com.example.group.model;

import com.example.course.Course;
import com.example.department.Department;
import com.example.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "groups") // Change 'group' to 'groups' to avoid SQL reserved keyword conflict
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @ManyToMany
    @JoinTable(
            name = "group_course",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>(); // Many-to-Many relationship

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = true)
    private Department department; // Belonging Department

    @ManyToOne
    @JoinColumn(name = "requesting_department_id", nullable = true)
    private Department requestingDepartment; // Requesting Department

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy; // Assuming User is your custom user entity

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return name;
    }
}

