package com.example.quiz.Request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TagDTO {
    private Long id;
    private String name;

    public TagDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // getters and setters
}