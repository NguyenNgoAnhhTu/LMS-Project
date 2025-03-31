package com.example.course.topic;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Integer> {
    Page<Topic> findAll(Pageable pageable);
    Page<Topic> findByTopicNameContainingIgnoreCase(String topicName, Pageable pageable);
    Optional<Topic> findByTopicName(String topicName);
}



