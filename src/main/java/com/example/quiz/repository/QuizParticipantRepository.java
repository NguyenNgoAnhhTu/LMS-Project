package com.example.quiz.repository;

import com.example.quiz.model.Quiz;
import com.example.quiz.model.QuizParticipant;
import com.example.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizParticipantRepository extends JpaRepository<QuizParticipant, Long> {
    Page<QuizParticipant> findByQuizId(Long quizId, Pageable pageable);

    @Query("SELECT qp FROM QuizParticipant qp " +
            "JOIN qp.user u " +
            "WHERE qp.quiz.id = :quizId " +
            "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<QuizParticipant> findByQuizIdAndSearchTerm(
            @Param("quizId") Long quizId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    Optional<QuizParticipant> findByQuizAndUser(Quiz quiz, User user);

    List<QuizParticipant> findAllByQuizAndUser(Quiz quiz, User user);

    List<QuizParticipant> findAllByQuizId(Long quizId);
    QuizParticipant findByQuizIdAndUserId(Long quizId, Long userId);


//    @Query("SELECT qp FROM quiz_participants qp WHERE " +
//            "(:firstName IS NULL OR LOWER(qp.app_user.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
//            "(:lastName IS NULL OR LOWER(qp.app_user.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')))")
//    List<QuizParticipant> findByFirstNameOrLastName(
//            @Param("firstName") String firstName,
//            @Param("lastName") String lastName);

    @Query("SELECT qp FROM QuizParticipant qp " +
           "JOIN qp.user u " +
           "WHERE qp.quiz.id = :quizId " +
           "AND (LOWER(u.firstName) LIKE LOWER(:searchTerm) " +
           "OR LOWER(u.lastName) LIKE LOWER(:searchTerm))")
    List<QuizParticipant> findByQuizIdAndUserName(
            @Param("quizId") Long quizId, 
            @Param("searchTerm") String searchTerm
    );

    @Query("SELECT qp FROM QuizParticipant qp " +
           "JOIN FETCH qp.quiz q " +
           "JOIN FETCH qp.testSession ts " +
           "WHERE q.id = :quizId AND ts.id = :testSessionId")
    Optional<QuizParticipant> findByQuizIdAndTestSessionId(@Param("quizId") Long quizId, 
                                                          @Param("testSessionId") Long testSessionId);
}
