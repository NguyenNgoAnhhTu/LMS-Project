package com.example.course.topic;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
@Entity
@Builder
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer topicId;

    @Column(nullable = false, unique = true, length = 100)
    private String topicName;

    @Override
    public String toString() {
        return topicName;
    }
}
