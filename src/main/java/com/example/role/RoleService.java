package com.example.role;

import com.example.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.example.utils.Helper;

import static com.example.utils.Helper.getCellValueAsString;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }
    public Role getRoleById(Integer id) {
        return roleRepository.findById(id).orElse(null);
    }

    public Role createRole(String roleName) {
        Role role = new Role();
        role.setName(roleName);
        return roleRepository.save(role);
    }

    public Role updateRole(Integer id, String roleName) {
        Optional<Role> existingRole = roleRepository.findById(id);
        if (existingRole.isPresent()) {
            Role role = existingRole.get();
            role.setName(roleName);
            return roleRepository.save(role);
        }
        return null;
    }

    public void deleteRole(Integer id)  {
        if(userRepository.existsByRolesContains(roleRepository.findById(id).get())) {
            throw new IllegalArgumentException("Cannot delete this role: Role is assigned to one or more users");
        }
        roleRepository.deleteById(id);
    }

    public void saveAll(List<Role> roles) {
        roleRepository.saveAll(roles);  // Save all roles at once
    }
    // Method to fetch paginated roles
    public Page<Role> getRoles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return roleRepository.findAll(pageable);
    }
    public Page<Role> searchRoles(String searchQuery, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return roleRepository.findByNameContainingIgnoreCase(searchQuery, pageable);
    }

    public List<Role> importExcel(MultipartFile file) throws Exception {
        Workbook workbook = new XSSFWorkbook(file.getInputStream()) ;
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getLastRowNum() + 1;
            List<Role> roles = new ArrayList<>();

            for (int i = 1; i < rowCount; i++) { // Start from 1 to skip the header row
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(1); // Assume role name is in column 1
                    if (cell != null) {
                        String roleName = getCellValueAsString(cell).trim(); // Use helper function

                        // Check if the role already exists
                        if (!roleExists(roleName)) {
                            Role role = new Role();
                            role.setName(roleName); // Correctly set the role name
                            roles.add(role);
                        }
                        else throw new IllegalArgumentException("Role " + roleName + " already exists");
                    }
                }
            }

            // Save roles to the database
            return roles; // Assuming you have a roleRepository bean
    }

    private boolean roleExists(String roleName) {
        return roleRepository.findByName(roleName).isPresent(); // Assuming you have this method in RoleRepository
    }

    // Method to export roles to Excel
    public ByteArrayInputStream exportRolesToExcel(List<Role> roles) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Roles");

        // Create the header row
        String[] headers = { "Role ID", "Role Name" };
        sheet.createRow(0).createCell(0).setCellValue(headers[0]);
        sheet.getRow(0).createCell(1).setCellValue(headers[1]);

        // Populate data rows
        int rowNum = 1;
        for (Role role : roles) {
            sheet.createRow(rowNum).createCell(0).setCellValue(role.getId());
            sheet.getRow(rowNum).createCell(1).setCellValue(role.getName());
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

    public boolean isRoleNameExists(String roleName) {
        return this.roleExists(roleName);
    }
}
