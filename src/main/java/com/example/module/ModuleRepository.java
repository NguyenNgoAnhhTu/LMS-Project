package com.example.module;

import com.example.module_group.ModuleGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends PagingAndSortingRepository<Module, Long> {

    @Query("SELECT m FROM Module m WHERE m.name LIKE %:searchQuery%")
    Page<Module> searchModules(@Param("searchQuery") String searchQuery, Pageable pageable);

    Page<Module> findAll(Pageable pageable);

    List<Module> findAll();

    Optional<Module> findById(Long id);

    Module save(Module module);

    boolean existsByName(String name);
    void deleteById(Long id);

}
