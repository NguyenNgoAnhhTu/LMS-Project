package com.example.code_judgement;

import com.example.testcase.TestCaseResult;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExecutionResponse {
    String code;
    int passed;
    int total;
    List<TestCaseResult> testCasesResults;
}
