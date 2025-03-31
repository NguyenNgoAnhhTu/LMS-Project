package com.example.assessment.repository;

import com.example.assessment.model.Assessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentRepository extends PagingAndSortingRepository<Assessment, Long> {

    @Query("SELECT a FROM Assessment a WHERE a.title LIKE %:searchQuery%")
    Page<Assessment> search(@Param("searchQuery") String searchQuery, Pageable pageable);

    Page<Assessment> findAll(Pageable pageable);

    List<Assessment> findAll();

    Optional<Assessment> findById(Long id);

    Assessment save(Assessment assessment);

    boolean existsByTitle(String title);

    void deleteById(Long id);
}

