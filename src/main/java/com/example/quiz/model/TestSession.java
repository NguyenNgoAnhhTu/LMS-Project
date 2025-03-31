package com.example.quiz.model;

import com.example.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "test_session")
public class TestSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assessment_id")
    private Long assessmentId; // Không gán mặc định bằng quizId

    @Column(name = "check_practice", nullable = false)
    private boolean checkPractice; // Đảm bảo không null trong cơ sở dữ liệu

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @OneToMany(mappedBy = "testSession", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Result> results;

    @OneToMany(mappedBy = "testSession", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers;
    @OneToMany(mappedBy = "testSession", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PracticeResult> practiceResults;

    // Constructor với Quiz để gán checkPractice
    public TestSession(User user, Quiz quiz, Long assessmentId) {
        this.user = user;
        this.startTime = LocalDateTime.now();
        this.assessmentId = assessmentId; // Lấy từ request
        setCheckPracticeBasedOnQuiz(quiz);
    }

    // Phương thức gán checkPractice dựa trên quizCategory
    private void setCheckPracticeBasedOnQuiz(Quiz quiz) {
        this.checkPractice = (quiz.getQuizCategory() == Quiz.QuizCategory.PRACTICE);
    }
}