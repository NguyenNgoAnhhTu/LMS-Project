package com.example.quiz.repository;

import com.example.quiz.model.QuizTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizTagRepository extends JpaRepository<QuizTag, Long> {
    Optional<QuizTag> findById(Long id);

    List<QuizTag> findByNameContainingIgnoreCase(String name);

    List<QuizTag> findAllById(Iterable<Long> ids);
}