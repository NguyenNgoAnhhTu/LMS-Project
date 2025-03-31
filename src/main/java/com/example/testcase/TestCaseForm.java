package com.example.testcase;

import lombok.*;

@Setter
@Getter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestCaseForm {
    // Getters and Setters
    private String input;
    private String expectedOutput;

}