package com.example.testcase;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseFormList {
    // Getters and Setters
    private List<TestCaseForm> testCasesList = new ArrayList<>();

}