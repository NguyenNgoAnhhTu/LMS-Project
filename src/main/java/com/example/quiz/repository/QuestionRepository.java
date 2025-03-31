package com.example.quiz.repository;

import com.example.quiz.model.Question;
import com.example.quiz.model.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // Find all questions belonging to a specific quiz
    List<Question> findByQuizzes_Id(Long quizId);

    // Custom query for searching questions by text
    @Query("SELECT q FROM Question q WHERE LOWER(q.questionText) LIKE LOWER(CONCAT('%', :searchQuery, '%'))")
    Page<Question> search(String searchQuery, Pageable pageable);

    // Find questions based on the name of their associated quiz
    //@Query("SELECT q FROM Question q JOIN q.quizzes quiz WHERE quiz.id = :quizId")
    //public abstract List<Question> findQuestionsByQuizId(@Param("quizId") Long quizId);

    List<Question> findByQuizzes_Name(String name);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.quizzes = :quiz")
    int countByQuiz(@Param("quiz") Quiz quiz);

    @Query("SELECT q FROM Question q WHERE q.quizzes = :quiz ORDER BY q.questionNo ASC")
    List<Question> findByQuizOrderByQuestionNo(@Param("quiz") Quiz quiz);

    List<Question> findByQuizzesOrderByQuestionNo(Quiz quiz);

    @Modifying
    @Transactional
    @Query("UPDATE Question q SET q.questionNo = q.questionNo + 1 WHERE q.quizzes = :quiz AND q.questionNo >= :newPos AND q.questionNo < :oldPos")
    void incrementQuestionNo(@Param("quiz") Quiz quiz, @Param("newPos") int newPos, @Param("oldPos") int oldPos);

    @Modifying
    @Transactional
    @Query("UPDATE Question q SET q.questionNo = q.questionNo - 1 WHERE q.quizzes = :quiz AND q.questionNo > :oldPos AND q.questionNo <= :newPos")
    void decrementQuestionNo(@Param("quiz") Quiz quiz, @Param("oldPos") int oldPos, @Param("newPos") int newPos);

    @Query("SELECT q FROM Question q JOIN q.quizzes quiz WHERE quiz.id = :quizId")
    public abstract List<Question> findQuestionsByQuizId(@Param("quizId") Long quizId);
    long count();
}