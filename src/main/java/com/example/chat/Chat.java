package com.example.chat;

import com.example.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "group_chat_id")
    private GroupChat group;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    private LocalDateTime editedAt;

    @Column(nullable = false)
    private boolean isRead = false;  // Track if the message has been read

    @Override
    public String toString() {
        if (group != null) {
            return sender.getUsername() + " -> Group " + group.getName() + ": " + message.substring(0, Math.min(20, message.length()));
        }
        return sender.getUsername() + " -> " + receiver.getUsername() + ": " + message.substring(0, Math.min(20, message.length()));
    }

    // Getters and setters omitted for brevity
}
