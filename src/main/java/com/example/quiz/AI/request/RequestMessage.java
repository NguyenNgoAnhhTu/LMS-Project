package com.example.quiz.AI.request;

import lombok.Data;

@Data
public class RequestMessage {
    private String role;
    private String content;

    public RequestMessage(String content) {
        role = "user";
        this.content = content;
    }
}
