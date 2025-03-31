package com.example.quiz.service;

import com.example.exception.NotFoundException;
import com.example.quiz.model.Result;
import com.example.quiz.repository.ResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ResultService {

    @Autowired
    private ResultRepository resultRepository;

    public List<Result> findAll() {
        return resultRepository.findAll();
    }

    public Optional<Result> findById(Long id) {
        return resultRepository.findById(id);
    }

    public Result createResult(Result result) {
        return resultRepository.save(result);
    }

    public void update(Long id, Result result) {
        Result existingResult = resultRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Result not found!"));

        existingResult.setScore(result.getScore());

        resultRepository.save(existingResult);
    }

    public void deleteById(Long id) {
        resultRepository.deleteById(id);
    }


}
