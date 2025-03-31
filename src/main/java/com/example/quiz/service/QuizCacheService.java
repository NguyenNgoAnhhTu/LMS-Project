package com.example.quiz.service;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
@Service
public class QuizCacheService {

    private final ConcurrentHashMap<Long, Object> quizCache = new ConcurrentHashMap<>();

    public void clearQuizCache(Long quizId) {
        quizCache.remove(quizId);
        System.out.println("Cache cleared for Quiz ID: " + quizId);
    }
}
