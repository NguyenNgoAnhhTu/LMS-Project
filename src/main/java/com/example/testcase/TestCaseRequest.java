package com.example.testcase;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TestCaseRequest {
    private String testCaseMethod;
    private String testCasesJson = null;
    private TestCaseFormList testCaseFormList = new TestCaseFormList();
}
