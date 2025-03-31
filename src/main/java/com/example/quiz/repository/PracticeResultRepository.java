package com.example.quiz.repository;

import com.example.quiz.model.PracticeResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PracticeResultRepository extends JpaRepository<PracticeResult, Long> {
    List<PracticeResult> findResultByTestSession_User_Id(Long userId);
}