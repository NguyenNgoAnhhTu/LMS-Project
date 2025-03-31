package com.example.quiz.repository;

import com.example.quiz.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    // Thêm các phương thức tùy chỉnh nếu cần
    List<Result> findResultByTestSession_IdIn(List<Long> testSessionIds);
}
