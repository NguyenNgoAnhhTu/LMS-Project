package com.example.quiz.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "question")
public class Question implements Cloneable{

    public enum QuestionType {
        MCQ, SCQ, TEXT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToMany
//    @JoinTable(
//            name = "quiz_question",
//            joinColumns = @JoinColumn(name = "question_id"),
//            inverseJoinColumns = @JoinColumn(name = "quiz_id")
//    )
//    private Set<Quiz> quizzes = new HashSet<>();

    public void addAnswerOption(AnswerOption answerOption) {
        answerOptions.add(answerOption);
        answerOption.setQuestion(this);
    }

//    @ManyToMany(mappedBy = "questions")
//    private Set<Quiz> quizzes = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    @JsonIgnore
    private Quiz quizzes;

    @Column(name = "question_no")
    private Integer questionNo;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @JsonProperty("questionType")
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", length = 50, nullable = false)
    private QuestionType questionType;

    @Column(name = "points", nullable = false)
    private Integer points = 0;


//    public void addQuiz(Quiz quiz) {
//        this.quizzes.add(quiz);
//        quiz.getQuestions().add(this);
//    }

    @JsonIgnore
    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    @JsonManagedReference("questions-answerOption")
    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnswerOption> answerOptions = new ArrayList<>();

    // Getters and Setters
    // Omitted for brevity

    @Override
    public Object clone() {
        try {
            Question cloned = (Question) super.clone();
            cloned.id = null; // Xóa ID để tránh lỗi trùng khóa
            cloned.quizzes = null;
            cloned.answers = null;
            cloned.questionNo = null;
            cloned.answerOptions = new ArrayList<>();
            for (AnswerOption answerOption : this.answerOptions) {
                AnswerOption clonedAnswerOption = (AnswerOption) answerOption.clone();
                clonedAnswerOption.setQuestion(cloned); // Liên kết D mới với C mới
                cloned.answerOptions.add(clonedAnswerOption);
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone failed", e);
        }
    }


}