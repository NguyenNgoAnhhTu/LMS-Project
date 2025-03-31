package com.example.department;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.exception.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;


    // Method to fetch all departments
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    // Method to fetch a department by ID
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(Math.toIntExact(id)).orElse(null);
    }

    // Method to create a new department
    public Department createDepartment(Department department) throws ObjectAlreadyExistsException {
        if (isDepartmentNameExists(department.getName())) {
            throw new ObjectAlreadyExistsException("Department with name '" + department.getName() + "' already exists");
        }
        return departmentRepository.save(department);
    }

    // Method to update an existing department
    public Department updateDepartment(Long id, Department department) throws ObjectAlreadyExistsException {
        Optional<Department> existingDepartment = departmentRepository.findById(Math.toIntExact(id));

        if (existingDepartment.isPresent()) {
            Department updatedDepartment = existingDepartment.get();
            if (!updatedDepartment.getName().equals(department.getName()) && isDepartmentNameExists(department.getName())) {
                throw new ObjectAlreadyExistsException("Department with name '" + department.getName() + "' already exists");
            }
            updatedDepartment.setName(department.getName());
            updatedDepartment.setLocation(department.getLocation());
            updatedDepartment.setUsers(department.getUsers());
            updatedDepartment.setCourses(department.getCourses());
            return departmentRepository.save(updatedDepartment);
        }
        return null;
    }


    // Method to delete a department by ID
    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(Math.toIntExact(id));
    }

    // Method to fetch paginated departments
    public Page<Department> getDepartments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return departmentRepository.findAll(pageable);
    }

    // Method to import departments from an Excel file
    public List<Department> importExcel(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            List<Department> departments = new ArrayList<>();

            for (int i = 1; i < rowCount; i++) { // Start from 1 to skip the header row
                Row row = sheet.getRow(i);
                if (row != null) {
                    // Assume department name is in column 0, department code in column 1
                    String departmentName = row.getCell(0).getStringCellValue().trim();

                    // Create a new Department object and add it to the list
                    Department department = new Department();
                    department.setName(departmentName);
                    departments.add(department);
                }
            }
            // Save departments to the database
            return departmentRepository.saveAll(departments);
        } catch (IOException e) {
            throw new RuntimeException("Error importing departments from Excel", e);
        }
    }

    // Method to search departments by name
    public Page<Department> searchDepartments(String searchQuery, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return departmentRepository.findByNameContainingIgnoreCase(searchQuery, pageable);
    }

    // Method to export departments to an Excel file
    public ByteArrayInputStream exportDepartmentsToExcel(List<Department> departments) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Departments");

        // Create the header row
        String[] headers = {"Department ID", "Department Name", "Location", "Number of Users", "Number of Courses"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Populate data rows
        int rowNum = 1;
        for (Department department : departments) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(department.getId());
            row.createCell(1).setCellValue(department.getName());
            row.createCell(2).setCellValue(department.getLocation() != null ? department.getLocation().getName() : "N/A");
            row.createCell(3).setCellValue(department.getUsers() != null ? department.getUsers().size() : 0);
            row.createCell(4).setCellValue(department.getCourses() != null ? department.getCourses().size() : 0);
        }

        // Write the workbook to a byte array output stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException("Error exporting departments to Excel", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }


    // Method to check if a department name already exists
    public boolean isDepartmentNameExists(String departmentName) {
        return departmentRepository.findByName(departmentName).isPresent();
    }

    public void saveAll(List<Department> departments) {
        departmentRepository.saveAll(departments);  // Save all roles at once
    }

    //for report
    public int getTotalCoursesInDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .map(department -> department.getCourses().size())
                .orElse(0);
    }

    public Optional<Department> findById(Long departmentId) {
        return departmentRepository.findById(departmentId);
    }

}