package com.example.group.repository;

import com.example.group.model.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends PagingAndSortingRepository<Group, Long> {

    @Query("SELECT m FROM Group m WHERE m.name LIKE %:searchQuery%")
    Page<Group> search(@Param("searchQuery") String searchQuery, Pageable pageable);

    Page<Group> findAll(Pageable pageable);

    List<Group> findAll();

    Optional<Group> findById(Long id);

    Group save(Group module);

    boolean existsByName(String name);
    void deleteById(Long id);
}

