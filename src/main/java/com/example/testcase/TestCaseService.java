package com.example.testcase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestCaseService {

    private final TestCaseRepository testCaseRepository;

    // 🟢 Batch save để lưu nhiều test cases cùng lúc
    public void saveAll(List<TestCase> testCases) {
        testCaseRepository.saveAll(testCases);
    }
//    public List<TestCase> findAll() {return testCaseRepository.findAll();}
}