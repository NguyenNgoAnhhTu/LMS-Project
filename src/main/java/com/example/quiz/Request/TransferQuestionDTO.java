package com.example.quiz.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferQuestionDTO {
    private Long questionId;
    private Long targetQuizId; // Thay vì targetQuizName

    // Getters & Setters
    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getTargetQuizId() {
        return targetQuizId;
    }

    public void setTargetQuizId(Long targetQuizId) {
        this.targetQuizId = targetQuizId;
    }
}
