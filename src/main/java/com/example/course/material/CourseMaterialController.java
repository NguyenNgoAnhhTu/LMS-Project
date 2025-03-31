package com.example.course.material;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/course-materials")
public class CourseMaterialController {
    @Autowired
    private CourseMaterialService courseMaterialService;

    @GetMapping
    public ResponseEntity<?> getAllMaterials(@RequestParam Long sectionId) {
        try {
            return ResponseEntity.ok(courseMaterialService.findAllBySectionId(sectionId));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
