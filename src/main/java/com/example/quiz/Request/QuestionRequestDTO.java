package com.example.quiz.Request;

import com.example.quiz.model.Question;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QuestionRequestDTO {
    private String questionText;
    private Question.QuestionType questionType;
    public Question.QuestionType getQuestionType() {
        return questionType != null ? questionType : Question.QuestionType.MCQ;
    }
    private List<AnswerOptionRequestDTO> answerOptions;
}
