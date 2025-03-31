package com.example.quiz.repository;

import com.example.quiz.model.AnswerOption;
import com.example.quiz.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {
    @Query("SELECT ao FROM AnswerOption ao WHERE ao.question.id = :questionId AND ao.isCorrect = true")
    AnswerOption findCorrectAnswerByQuestionId(@Param("questionId") Long questionId);

    @Transactional
    void deleteAllByQuestion(Question question);
    @Query("SELECT ao FROM AnswerOption ao WHERE ao.question.id = :questionId AND ao.isCorrect = true")
    List<AnswerOption> findCorrectAnswersByQuestionId(@Param("questionId") Long questionId);

    @Query("SELECT ao FROM AnswerOption ao WHERE ao.question.id IN :questionIds AND ao.isCorrect = true")
    List<AnswerOption> findCorrectAnswersByQuestionIds(@Param("questionIds") List<Long> questionIds);

    @Query("SELECT COUNT(ao) FROM AnswerOption ao WHERE ao.isCorrect = true")
    long countTotalCorrectAnswers();

    @Query("SELECT COUNT(ao) FROM AnswerOption ao WHERE ao.isCorrect = false")
    long countTotalIncorrectAnswers();

    List<AnswerOption> findByQuestionId(long id);

}
