package com.example.reports.service;

import com.example.department.DepartmentRepository;
import com.example.course.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    public long getTotalDepartments() {
        return departmentRepository.count();
    }

    public long getTotalCourses() {
        return courseRepository.count();
    }

    // Add more methods for other reports like assessments, users, etc.
}
