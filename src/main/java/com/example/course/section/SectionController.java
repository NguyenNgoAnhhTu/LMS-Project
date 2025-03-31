package com.example.course.section;

import com.example.course.material.CourseMaterialDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sections")
@RequiredArgsConstructor
public class SectionController {
    private final SectionService sectionService;

    @PostMapping("/edit/{id}")
    public ResponseEntity<?> editSection(@PathVariable("id") Long id, @ModelAttribute Section newSection) {
        try {
            Section section = sectionService.getSectionById(id);
            section.setName(newSection.getName());
            System.out.println(section);
            return ResponseEntity.ok(sectionService.updateSection(section, id));
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createSection(@ModelAttribute Section newSection, @RequestParam Long courseId) {
        try {
            return ResponseEntity.ok(sectionService.createSection(newSection, courseId));
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllSections(@RequestParam Long courseId) {
        try {
            List<Section> sections = sectionService.getSectionsByCourse(courseId);
            List<Map<String, Object>> response = new ArrayList<>();
            for (Section section : sections) {
                Map<String, Object> map = new HashMap<>();
                map.put("sectionId", section.getId());
                map.put("sectionName", section.getName());
                map.put("courseMaterials", section.getCourseMaterials().stream().map(CourseMaterialDTO::toDTO).toList());
                response.add(map);
            }
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
