package com.example.testcase;

import com.example.exercise.Exercise;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String input;

    private String expectedOutput;


    @ManyToOne
    @JoinColumn(name = "exercise_id" ,referencedColumnName = "id", nullable = false)
    private Exercise exercise;

    @Override
    public String toString() {
        return "TestCase " + id + ": Input: " + input + " | Expected Output: " + expectedOutput;
    }
}