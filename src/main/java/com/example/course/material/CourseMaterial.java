package com.example.course.material;

import com.example.course.section.Section;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class CourseMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public enum MaterialType {
        ASSIGNMENTS, LABS, LECTURES, REFERENCES, ASSESSMENTS
    }

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = true)
    @JsonBackReference
    private Section section;

    private String materialId;  // Assuming it's a unique identifier for the material

    @Enumerated(EnumType.STRING)
    private MaterialType materialType;

    private String materialName;
    private String materialUrl;

    private int orderNum;

    private String title;

    private Float expectDuration;

    private int wordCount;

    @Override
    public String toString() {
        return String.format("Session ID: %d   Title: %s", section.getId(), title);
    }
}
