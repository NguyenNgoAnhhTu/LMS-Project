package com.example.group.service;

import com.example.course.Course;
import com.example.course.CourseService;
import com.example.department.Department;
import com.example.department.DepartmentRepository;
import com.example.group.model.Group;
import com.example.group.repository.GroupRepository;
import com.example.user.User;
import com.example.user.UserService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.utils.Helper.getCellValueAsString;


@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;
    
    public Optional<Group> findById(Long id) {
        return groupRepository.findById(id);
    }

    public Group save(Group group) {
        return groupRepository.save(group);
    }

    public void deleteById(Long id) {
        groupRepository.deleteById(id);
    }

    public Page<Group> findAll(Pageable pageable) {
        return groupRepository.findAll(pageable);  // Lấy tất cả groups với phân trang
    }

    public List<Group> findAll() {
        return groupRepository.findAll();
    }

    public Page<Group> search(String searchQuery, Pageable pageable) {
        return groupRepository.search(searchQuery, pageable);  // Tìm kiếm với phân trang
    }

    boolean existsByName(String groupName) {
        return groupRepository.existsByName(groupName);
    }
	//will move to utils later
    public User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Get the username of the logged-in user
        return userService.findByUsername(username);
    }

    public void importExcel(MultipartFile file) {
        // Get the authenticated user's details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Get the username of the logged-in user
		User currentUser = userService.findByUsername(username);

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            List<Group> groups = new ArrayList<>();

            for (int i = 1; i < rowCount; i++) { // Start from 1 to skip the header row
                Row row = sheet.getRow(i);
                if (row != null) {
                    // Extract cells
                    Cell nameCell = row.getCell(0); // Group Name
                    Cell coursesCell = row.getCell(1); // Course Names (Comma-separated)
                    Cell departmentCell = row.getCell(2); // Department Name
                    Cell requestingDepartmentCell = row.getCell(3); // Requesting Department Name
                    Cell createdAtCell = row.getCell(4); // Created At

                    if (nameCell != null && departmentCell != null) {
                        // Get group name
                        String groupName = getCellValueAsString(nameCell).trim();

                        // Get department
                        String departmentName = getCellValueAsString(departmentCell).trim();
                        Department department = findDepartmentByName(departmentName); // Implement this method

                        // Get requesting department
                        Department requestingDepartment = null;
                        if (requestingDepartmentCell != null) {
                            String requestingDepartmentName = getCellValueAsString(requestingDepartmentCell).trim();
                            requestingDepartment = findDepartmentByName(requestingDepartmentName); // Implement this method
                        }

                        // Parse createdAt (if present)
                        LocalDateTime createdAt = null;
                        if (createdAtCell != null) {
                            String createdAtString = getCellValueAsString(createdAtCell).trim();
                            if (!createdAtString.isEmpty()) {
                                createdAt = LocalDateTime.parse(createdAtString); // Ensure the format matches ISO-8601
                            }
                        }

                        // Get courses (comma-separated course names)
                        Set<Course> courses = new HashSet<>();
                        if (coursesCell != null) {
                            String coursesString = getCellValueAsString(coursesCell).trim();
                            if (!coursesString.isEmpty()) {
                                String[] courseNames = coursesString.split(",");
                                for (String courseName : courseNames) {
                                    Course course = courseService.findByName(courseName.trim()); // Implement this method
                                    if (course != null) {
                                        courses.add(course);
                                    }
                                }
                            }
                        }

                        // Check if the group already exists
                        if (!groupRepository.existsByName(groupName)) {
                            // Create and populate the group object
                            Group group = new Group();
                            group.setName(groupName);
                            group.setCourses(courses);
                            group.setDepartment(department);
                            group.setRequestingDepartment(requestingDepartment);
                            group.setCreatedAt(createdAt != null ? createdAt : LocalDateTime.now());
                            group.setCreatedBy(currentUser); // Set the current user as the creator

                            groups.add(group);
                        }
                    }
                }
            }

            // Save groups to the database
            for (Group group : groups) {
                groupRepository.save(group);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error importing groups from Excel", e);
        }
    }


    private Department findDepartmentByName(String groupName) {
        return departmentRepository.findByName(groupName).orElseThrow(() -> new RuntimeException("Group not found"));
    }


    public ByteArrayInputStream exportToExcel(List<Group> groups) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Groups");

        // Create the header row
        String[] headers = {
                "ID", "Name", "Courses", "Department", "Requesting Department", "Created At", "Created By"
        };
        sheet.createRow(0).createCell(0).setCellValue(headers[0]);
        sheet.getRow(0).createCell(1).setCellValue(headers[1]);
        sheet.getRow(0).createCell(2).setCellValue(headers[2]);
        sheet.getRow(0).createCell(3).setCellValue(headers[3]);
        sheet.getRow(0).createCell(4).setCellValue(headers[4]);
        sheet.getRow(0).createCell(5).setCellValue(headers[5]);
        sheet.getRow(0).createCell(6).setCellValue(headers[6]);

        // Populate data rows
        int rowNum = 1;
        for (Group group : groups) {
            sheet.createRow(rowNum).createCell(0).setCellValue(group.getId());
            sheet.getRow(rowNum).createCell(1).setCellValue(group.getName());

            // Export courses as a comma-separated list
            String courses = group.getCourses().stream()
                    .map(Course::getName) // Assuming `Course` class has a `getName` method
                    .collect(Collectors.joining(", "));
            sheet.getRow(rowNum).createCell(2).setCellValue(courses);

            // Export department name
            sheet.getRow(rowNum).createCell(3).setCellValue(group.getDepartment() != null ? group.getDepartment().getName() : "");

            // Export requesting department name (if available)
            sheet.getRow(rowNum).createCell(4).setCellValue(group.getRequestingDepartment() != null ? group.getRequestingDepartment().getName() : "");

            // Export createdAt as a string (ISO-8601 format)
            sheet.getRow(rowNum).createCell(5).setCellValue(group.getCreatedAt() != null ? group.getCreatedAt().toString() : "");

            // Export createdBy (username or other relevant field of the user)
            sheet.getRow(rowNum).createCell(6).setCellValue(group.getCreatedBy() != null ? group.getCreatedBy().getUsername() : "");

            rowNum++;
        }

        // Write to ByteArrayOutputStream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

}

