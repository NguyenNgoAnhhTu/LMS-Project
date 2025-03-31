package com.example.quiz.AI;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIRequestBody {

    private String type;
//    private String model;
    private int numOfQuestions;
    private int numOfAnswerOptions;
//    private int numOfCorrectAnswer;
    private String questionDescription;
}
