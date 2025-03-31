package com.example.quiz.AI.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Choice {
    private ResponseMessage message;
    private String finish_reason;
    private String native_finish_reason;
    private int index;
    private Object logprobs;

    public Choice(String content) {
        this.message = new ResponseMessage(content);
    }
}
