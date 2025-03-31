package com.example.quiz.service;

import com.example.quiz.model.Quiz;
import com.example.quiz.repository.QuizRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class QuizStatusCloseUpdaterJob implements Job {

    @Autowired
    private transient QuizRepository quizRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long quizId = (Long) context.getJobDetail().getJobDataMap().get("quizId");
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz != null) {
            quiz.setQuizType(Quiz.QuizType.CLOSE);
            quizRepository.save(quiz);
        }
    }
}
