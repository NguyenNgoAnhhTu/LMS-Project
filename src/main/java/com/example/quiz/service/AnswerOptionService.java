package com.example.quiz.service;

import com.example.quiz.model.AnswerOption;
import com.example.quiz.repository.AnswerOptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnswerOptionService {
    @Autowired
    private AnswerOptionRepository answerOptionRepository;

    public List<AnswerOption> getAnswerOptionByid(long id){
        return answerOptionRepository.findByQuestionId(id);
    }

}
