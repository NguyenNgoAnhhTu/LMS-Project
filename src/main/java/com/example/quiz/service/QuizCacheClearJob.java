package com.example.quiz.service;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class QuizCacheClearJob implements Job {

    @Autowired
    private QuizCacheService quizCacheService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long quizId = context.getJobDetail().getJobDataMap().getLong("quizId");

        if (quizId != null) {
            quizCacheService.clearQuizCache(quizId);
            System.out.println("Cache cleared for Quiz ID: " + quizId);
        }
    }
}
