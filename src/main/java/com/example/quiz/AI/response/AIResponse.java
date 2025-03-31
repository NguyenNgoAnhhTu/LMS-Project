package com.example.quiz.AI.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse {

    private String id;
    private String provider;
    private String model;
    private String object;
    private long created;
    private LinkedList<Choice> choices;
    private Usage usage;


    public AIResponse(String content) {
        this.choices = new LinkedList<>();
        choices.add(new Choice(content));
    }

}
