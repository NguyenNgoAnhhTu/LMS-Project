package com.example.quiz.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_session_id", nullable = false)
    @JsonIgnore
    private TestSession testSession;
    @Column(name = "score")
    private double score;

    @Column(name = "completion_time")
    private LocalDateTime completionTime;

    public Result(TestSession testSession, Integer score) {
        this.testSession = testSession;
        this.score = score;
        this.completionTime = LocalDateTime.now();
    }

    public Result(TestSession session, Question question, boolean isCorrect, double points) {
        this.testSession = session;
        this.score = isCorrect ? points : 0;
        this.completionTime = LocalDateTime.now();
    }

}