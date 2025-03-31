package com.example.quiz.service;

import com.example.quiz.model.QuizTag;
import com.example.quiz.repository.QuizTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class QuizTagService {

    @Autowired
    private QuizTagRepository quizTagRepository;

    public List<QuizTag> getAllQuizTag(){return quizTagRepository.findAll();}

    public QuizTag getQuizTagById(Long id){return quizTagRepository.findById(id).orElse(null);}

    public void save(QuizTag quizTag) {
        quizTagRepository.save(quizTag);
    }

    public Set<QuizTag> findAllById(List<Long> ids) {
        return new HashSet<>(quizTagRepository.findAllById(ids));
    }

    public QuizTag createTag(String name) {
        QuizTag newTag = new QuizTag();
        newTag.setName(name);
        return quizTagRepository.save(newTag);
    }

    public boolean isTagUsedByAnyQuiz(Long tagId) {
        QuizTag tag = getQuizTagById(tagId);
        return tag != null && !tag.getQuizzes().isEmpty();
    }

    public void deleteTagById(Long id) {
        if (isTagUsedByAnyQuiz(id)) {
            throw new IllegalStateException("Cannot delete tag because it is being used by one or more quizzes");
        }
        quizTagRepository.deleteById(id);
    }

    public List<QuizTag> searchTagsByName(String name) {
        return quizTagRepository.findByNameContainingIgnoreCase(name);
    }

    public boolean isTagUsedInQuiz(Long tagId) {
        QuizTag tag = getQuizTagById(tagId);
        if (tag == null) {
            return false;
        }
        return !tag.getQuizzes().isEmpty();
    }
}