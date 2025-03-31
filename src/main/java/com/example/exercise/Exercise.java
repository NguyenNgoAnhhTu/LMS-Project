package com.example.exercise;

import com.example.assessment.model.ProgrammingLanguage;
import com.example.testcase.TestCase;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {

    public enum Level { EASY, MEDIUM, HARD }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "programming_language_id", nullable = false)
    private ProgrammingLanguage language;

    @Column(columnDefinition = "TEXT")
    private String setup;

    @Enumerated(EnumType.STRING)
    private Level level = Level.EASY;

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestCase> testCases = new ArrayList<>();

    public void setTestCases(List<TestCase> testCases) {
        if (testCases == null) return;
        this.testCases.clear();
        for (TestCase testCase : testCases) {
            this.addTestCase(testCase);
        }
    }

    public void addTestCase(TestCase testCase) {
        if (testCase != null) {
            testCase.setExercise(this);
            testCases.add(testCase);
        }
    }
}