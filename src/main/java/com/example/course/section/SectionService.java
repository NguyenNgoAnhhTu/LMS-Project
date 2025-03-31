package com.example.course.section;

import com.example.course.Course;
import com.example.course.CourseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SectionService {
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private CourseRepository courseRepository;

    public List<Section> getAllSections() {
        return sectionRepository.findAll();
    }

    public List<Section> getSectionsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(EntityNotFoundException::new);
        return sectionRepository.findAllByCourse(course);
    }

    public Section getSectionById(Long id) {
        return sectionRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    public Section changeOrderNumber(Section section, Long newOrderNumber) {
        section.setOrderNumber(newOrderNumber);
        return sectionRepository.save(section);
    }

    public void deleteSectionById(Long id) {
        sectionRepository.deleteById(id);
    }

    public Section createSection(Section section, Long courseId) {
        section.setCourse(courseRepository.findById(courseId).orElseThrow(EntityNotFoundException::new));
        return sectionRepository.save(section);
    }

    public Section updateSection(Section section, Long sectionId) {
        Section oldSection = sectionRepository.findById(sectionId).orElseThrow(EntityNotFoundException::new);
        oldSection.setName(section.getName());
        return sectionRepository.save(oldSection);
    }
}
