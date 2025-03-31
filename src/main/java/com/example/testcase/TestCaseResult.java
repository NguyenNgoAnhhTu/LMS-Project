package com.example.testcase;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Helper class to store test case results
 */
@Getter
@Setter
@AllArgsConstructor
public class TestCaseResult {
    private TestCase testCase;
    private String userOutput;
    private boolean isCorrect;
}