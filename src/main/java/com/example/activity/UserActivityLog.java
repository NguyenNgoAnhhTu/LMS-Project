package com.example.activity;

import com.example.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    @Column(length = 500)
    private String activityDetails;

    @Column(nullable = false)
    private LocalDateTime activityTimestamp;

    public enum ActivityType {
        LOGIN, COURSE_COMPLETION, LOGOUT, PAGE_VISIT
    }

    public UserActivityLog(User user, ActivityType activityType, String activityDetails, LocalDateTime activityTimestamp) {
        this.user = user;
        this.activityType = activityType;
        this.activityDetails = activityDetails;
        this.activityTimestamp = activityTimestamp;
    }
}

