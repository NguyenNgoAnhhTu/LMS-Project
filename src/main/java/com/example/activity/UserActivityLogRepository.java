package com.example.activity;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    // Use Pageable for pagination
    Page<UserActivityLog> findByUser_Username(String username, Pageable pageable);

    // You can also add a method to retrieve all logs paginated
    Page<UserActivityLog> findAll(Pageable pageable);
}
