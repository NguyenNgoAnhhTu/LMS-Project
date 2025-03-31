package com.example.assessment.repository;


import com.example.assessment.model.ProgrammingLanguage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProgrammingLanguageRepository extends JpaRepository<ProgrammingLanguage, Integer> {

    @Query("SELECT m FROM ProgrammingLanguage m WHERE m.language LIKE %:searchQuery%")
    Page<Module> searchModules(@Param("searchQuery") String searchQuery, Pageable pageable);

    Page<ProgrammingLanguage> findAll(Pageable pageable);
    Page<ProgrammingLanguage> findByLanguageContainingIgnoreCase(String name, Pageable pageable);
    Optional<ProgrammingLanguage> findByLanguage(String name);
    ProgrammingLanguage save(ProgrammingLanguage programmingLanguage);
}


