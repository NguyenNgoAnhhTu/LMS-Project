package com.example.quiz.AI.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIRequest {
    String model;
    //    String prompt;
    //    boolean stream;
    LinkedList<RequestMessage> messages;

    public AIRequest(String model, String messages) {
        this.model = model;
        this.messages = new LinkedList<>();
        this.messages.add(new RequestMessage(messages));
    }
}
