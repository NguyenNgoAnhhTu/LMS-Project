package com.example.quiz.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "quiz_tag")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizTag {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "name", nullable = false, unique = true)
        private String name;

        @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
        @JsonIgnore
        private Set<Quiz> quizzes = new HashSet<>();

}
