package com.example.quiz.Request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferAllQuestionsDTO {
    private Long sourceQuizId;
    private Long targetQuizId;

    public Long getSourceQuizId() {
        return sourceQuizId;
    }

    public void setSourceQuizId(Long sourceQuizId) {
        this.sourceQuizId = sourceQuizId;
    }

    public Long getTargetQuizId() {
        return targetQuizId;
    }

    public void setTargetQuizId(Long targetQuizId) {
        this.targetQuizId = targetQuizId;
    }
}
