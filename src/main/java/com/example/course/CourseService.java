package com.example.course;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    // Method to fetch all courses
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // Method to fetch a course by ID
    public Course getCourseById(Long id) {
        return courseRepository.findById(id).orElse(null);
    }

    public Course findByName(String name) {
        return courseRepository.findByName(name).orElse(null);
    }

    // Method to create a new course
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    // Method to update an existing course
    public Course updateCourse(Long id, Course course) {
        Optional<Course> existingCourse = courseRepository.findById(id);
        if (existingCourse.isPresent()) {
            Course updatedCourse = existingCourse.get();
            updatedCourse.setName(course.getName());
            updatedCourse.setCode(course.getCode());
            updatedCourse.setDescription(course.getDescription());
            updatedCourse.setDurationInWeeks(course.getDurationInWeeks());
            updatedCourse.setLanguage(course.getLanguage());
            updatedCourse.setLevel(course.getLevel());
            updatedCourse.setPrice(course.getPrice());
            updatedCourse.setDiscount(course.getDiscount());
            updatedCourse.setPublished(course.isPublished());
//            updatedCourse.setInstructor(course.getInstructor());
//            updatedCourse.setCreator(course.getCreator());
//            updatedCourse.setPrerequisites(course.getPrerequisites());
//            updatedCourse.setTags(course.getTags());
//            updatedCourse.setImage(course.getImage());
            return courseRepository.save(updatedCourse);
        }
        return null;
    }

    // Method to delete a course by ID
    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    // Method to fetch paginated courses
    public Page<Course> getCourses(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return courseRepository.findAll(pageable);
    }

    // Method to search courses by name
    public Page<Course> searchCourses(String searchQuery, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return courseRepository.findByNameContainingIgnoreCase(searchQuery, pageable);
    }

    // Method to import courses from an Excel file
    public List<Course> importExcel(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            List<Course> courses = new ArrayList<>();

            for (int i = 1; i < rowCount; i++) { // Start from 1 to skip the header row
                Row row = sheet.getRow(i);
                if (row != null) {
                    // Assume course name is in column 0, course code in column 1
                    String courseName = row.getCell(0).getStringCellValue().trim();
                    String courseCode = row.getCell(1).getStringCellValue().trim();
                    float coursePrice = (float) row.getCell(2).getNumericCellValue();

                    // Create a new Course object and add it to the list
                    Course course = new Course();
                    course.setName(courseName);
                    course.setCode(courseCode);
                    course.setPrice(coursePrice);
                    courses.add(course);
                }
            }
            // Save courses to the database
            return courseRepository.saveAll(courses);
        } catch (IOException e) {
            throw new RuntimeException("Error importing courses from Excel", e);
        }
    }

    // Method to export courses to an Excel file
    public ByteArrayInputStream exportCoursesToExcel(List<Course> courses) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Courses");

        // Create the header row
        String[] headers = {"Course ID", "Course Name", "Course Code", "Price"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Populate data rows
        int rowNum = 1;
        for (Course course : courses) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(course.getId());
            row.createCell(1).setCellValue(course.getName());
            row.createCell(2).setCellValue(course.getCode());
            row.createCell(3).setCellValue(course.getPrice());
        }

        // Write the workbook to a byte array output stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    // Method to check if a course name already exists
    public boolean isCourseNameExists(String courseName) {
        return courseRepository.findByName(courseName).isPresent();
    }

    public void saveAll(List<Course> courses) {
        courseRepository.saveAll(courses);  // Save all roles at once
    }

    public Course save(Course course){
        return courseRepository.save(course);
    }
}

