package com.example.location;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Integer> {

    @Query("SELECT m FROM Location m WHERE m.name LIKE %:searchQuery%")
    Page<Module> searchModules(@Param("searchQuery") String searchQuery, Pageable pageable);

    Page<Location> findAll(Pageable pageable);
    Page<Location> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Location> findByName(String name);
    Location save(Location location);
}


