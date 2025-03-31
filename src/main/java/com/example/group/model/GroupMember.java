package com.example.group.model;

import com.example.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group; // Reference to CollaborationGroup

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Assuming User is your custom user entity

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onJoin() {
        this.joinedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return user.getUsername() + " - " + group.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupMember)) return false;
        GroupMember that = (GroupMember) o;
        return group.equals(that.group) && user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, user);
    }
}

