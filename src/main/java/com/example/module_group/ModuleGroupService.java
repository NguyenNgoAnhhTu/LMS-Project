package com.example.module_group;


import com.example.exception.ObjectAlreadyExistsException;
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

@Service
public class ModuleGroupService {

    @Autowired
    private ModuleGroupRepository moduleGroupRepository;

    public List<ModuleGroup> getAllModuleGroups() {
        return moduleGroupRepository.findAll();
    }

    public ModuleGroup getModuleGroupById(Long id) {
        return moduleGroupRepository.findById(id).orElse(null);
    }

    public ModuleGroup createModuleGroup(String ModuleGroupName) throws ObjectAlreadyExistsException {
        if (isModuleGroupNameExists(ModuleGroupName)) {
            throw new ObjectAlreadyExistsException("Module Group with name '" + ModuleGroupName + "' already exists");
        }
        ModuleGroup ModuleGroup = new ModuleGroup();
        ModuleGroup.setName(ModuleGroupName);
        return moduleGroupRepository.save(ModuleGroup);
    }

    public ModuleGroup updateModuleGroup(Long id, String ModuleGroupName) throws ObjectAlreadyExistsException {
        Optional<ModuleGroup> existingModuleGroup = moduleGroupRepository.findById(id);
        if (existingModuleGroup.isPresent()) {
            ModuleGroup ModuleGroup = existingModuleGroup.get();
            if (!ModuleGroup.getName().equals(ModuleGroupName) && isModuleGroupNameExists(ModuleGroupName)) {
                throw new ObjectAlreadyExistsException("Module Group with name '" + ModuleGroupName + "' already exists");
            }
            ModuleGroup.setName(ModuleGroupName);
            return moduleGroupRepository.save(ModuleGroup);
        }
        return null;
    }

    public void deleteModuleGroup(Long id) {
        moduleGroupRepository.deleteById(id);
    }

    public void saveAll(List<ModuleGroup> ModuleGroups) {
        moduleGroupRepository.saveAll(ModuleGroups);  // Save all ModuleGroups at once
    }
    // Method to fetch paginated ModuleGroups
    public Page<ModuleGroup> getModuleGroups(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return moduleGroupRepository.findAll(pageable);
    }
    public Page<ModuleGroup> searchModuleGroups(String searchQuery, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return moduleGroupRepository.findByNameContainingIgnoreCase(searchQuery, pageable);
    }

    // Import ModuleGroups from an Excel file
    public List<ModuleGroup> importExcel(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            List<ModuleGroup> ModuleGroups = new ArrayList<>();

            for (int i = 1; i < rowCount; i++) { // Start from 1 to skip the header row
                Row row = sheet.getRow(i);
                if (row != null) {
                    String ModuleGroupName = row.getCell(0).getStringCellValue(); // Assume ModuleGroup name is in column 0
                    Cell cell = row.getCell(0); // Assume ModuleGroup name is in column 0

                    if (cell != null) {
                        // Check if the ModuleGroup already exists
                        if (!moduleGroupExists(ModuleGroupName)) {
                            ModuleGroup ModuleGroup = new ModuleGroup();
                            ModuleGroup.setName(ModuleGroupName);
                            ModuleGroups.add(ModuleGroup);
                        }
                    }
                }
            }

            // Save ModuleGroups to the database
            return moduleGroupRepository.saveAll(ModuleGroups); // Assuming you have a moduleGroupRepository bean
        } catch (IOException e) {
            throw new RuntimeException("Error importing ModuleGroups from Excel", e);
        }
    }

    private boolean moduleGroupExists(String ModuleGroupName) {
        return moduleGroupRepository.findByName(ModuleGroupName).isPresent(); // Assuming you have this method in moduleGroupRepository
    }

    // Method to export ModuleGroups to Excel
    public ByteArrayInputStream exportModuleGroupsToExcel(List<ModuleGroup> ModuleGroups) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("ModuleGroups");

        // Create the header row
        String[] headers = { "ModuleGroup ID", "ModuleGroup Name" };
        sheet.createRow(0).createCell(0).setCellValue(headers[0]);
        sheet.getRow(0).createCell(1).setCellValue(headers[1]);

        // Populate data rows
        int rowNum = 1;
        for (ModuleGroup ModuleGroup : ModuleGroups) {
            sheet.createRow(rowNum).createCell(0).setCellValue(ModuleGroup.getId());
            sheet.getRow(rowNum).createCell(1).setCellValue(ModuleGroup.getName());
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

    public boolean isModuleGroupNameExists(String ModuleGroupName) {
        return moduleGroupExists(ModuleGroupName);
    }
}
