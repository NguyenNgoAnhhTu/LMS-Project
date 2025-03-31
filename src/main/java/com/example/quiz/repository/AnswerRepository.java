package com.example.quiz.repository;

import com.example.quiz.model.Answer;
import com.example.quiz.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    @Transactional
    void deleteAllByQuestion(Question question);

    @Query("SELECT COUNT(a) FROM Answer a WHERE a.isCorrect = true")
    long countTotalCorrectAnswers();

    // Đếm tổng số câu trả lời sai
    @Query("SELECT COUNT(a) FROM Answer a WHERE a.isCorrect = false")
    long countTotalIncorrectAnswers();

    List<Answer> findByTestSessionId(Long testSessionId);
}
