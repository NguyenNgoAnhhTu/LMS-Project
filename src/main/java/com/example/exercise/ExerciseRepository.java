package com.example.exercise;

import com.example.testcase.TestCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends PagingAndSortingRepository<Exercise, Long> {

    @Query("SELECT e FROM Exercise e WHERE e.name LIKE %:searchQuery%")
    Page<Exercise> searchExercises(@Param("searchQuery") String searchQuery, Pageable pageable);

    @Query("SELECT e FROM Exercise e " +
            "WHERE (:languageId IS NULL OR e.language.id = :languageId) " +
            "AND (:level IS NULL OR e.level = :level)")
    Page<Exercise> findByFilters(@Param("languageId") Long languageId,
                                 @Param("level") Exercise.Level level,
                                 Pageable pageable);

    Page<Exercise> findAll(Pageable pageable);

    List<Exercise> findAll();

    Optional<Exercise> findById(Long id);

    Exercise save(Exercise exercise);

    boolean existsByName(String name);

    void deleteById(Long id);

}