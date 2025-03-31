package com.example.quiz.AI.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponseMessage {
    private String role;
    private String content;
    private Object refusal;

    public ResponseMessage(String content){
        this.role = "assistant";
        this.content = getJson(content);
        this.refusal = null;
    }

    public void setContent(String content) {
        this.content = getJson(content);
    }

    private String getJson(String content){
        int startJson = content.indexOf("{");
        int endJson = content.lastIndexOf("}") + 1;

        if (startJson != -1 && endJson != -1) {
            return  content.substring(startJson, endJson);
        }

        return content;
    }
}
