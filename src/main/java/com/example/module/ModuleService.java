package com.example.module;

import com.example.module_group.ModuleGroup;
import com.example.module_group.ModuleGroupRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.utils.Helper.getCellValueAsString;

@Service
public class ModuleService {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ModuleGroupRepository moduleGroupRepository;

    public Optional<Module> getModuleById(Long id) {
        return moduleRepository.findById(id);
    }

    public Module saveModule(Module module) {
        return moduleRepository.save(module);
    }

    public void deleteModule(Long id) {
        moduleRepository.deleteById(id);
    }

    public List<ModuleGroup> getAllModuleGroups() {
        return moduleGroupRepository.findAll();
    }

    public Page<Module> getAllModules(Pageable pageable) {
        return moduleRepository.findAll(pageable);  // Lấy tất cả modules với phân trang
    }

    public List<Module> findAllModules() {
        return moduleRepository.findAll();
    }

    public Page<Module> searchModules(String searchQuery, Pageable pageable) {
        return moduleRepository.searchModules(searchQuery, pageable);  // Tìm kiếm với phân trang
    }

    boolean moduleExists(String moduleName) {
        return moduleRepository.existsByName(moduleName);
    }

    public void importExcel(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            List<Module> modules = new ArrayList<>();

            for (int i = 1; i < rowCount; i++) { // Start from 1 to skip the header row
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell idCell = row.getCell(0);
                    Cell nameCell = row.getCell(1);
                    Cell urlCell = row.getCell(2);
                    Cell iconCell = row.getCell(3);
                    Cell groupCell = row.getCell(4);

                    if (nameCell != null && urlCell != null) {
                        String moduleName = getCellValueAsString(nameCell).trim(); // Get module name
                        String moduleUrl = getCellValueAsString(urlCell).trim(); // Get module URL
                        String moduleIcon = iconCell != null ? getCellValueAsString(iconCell).trim() : null; // Get module icon, if present
                        String groupName = groupCell != null ? getCellValueAsString(groupCell).trim() : null; // Get module group, if present

                        // Check if module already exists based on some criteria (e.g., module name)
                        if (!moduleExists(moduleName)) {
                            Module module = new Module();
                            module.setName(moduleName);
                            module.setUrl(moduleUrl);
                            module.setIcon(moduleIcon);

                            if (groupName != null) {
                                ModuleGroup moduleGroup = findModuleGroupByName(groupName); // Implement this method based on your logic
                                module.setModuleGroup(moduleGroup);
                            }

                            modules.add(module);
                        }
                    }
                }
            }

            // Save modules to the database
           for (Module module : modules) {
               moduleRepository.save(module);
           }
        } catch (IOException e) {
            throw new RuntimeException("Error importing modules from Excel", e);
        }
    }

    private ModuleGroup findModuleGroupByName(String groupName) {
        return moduleGroupRepository.findByName(groupName).orElseThrow(() -> new RuntimeException("Group not found"));
    }


    // Method to export roles to Excel
    public ByteArrayInputStream exportModulesToExcel(List<Module> modules) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Modules");

        // Create the header row
        String[] headers = { "ID", "Name", "Url", "Icon", "Module Group" };
        sheet.createRow(0).createCell(0).setCellValue(headers[0]);
        sheet.getRow(0).createCell(1).setCellValue(headers[1]);
        sheet.getRow(0).createCell(2).setCellValue(headers[2]);
        sheet.getRow(0).createCell(3).setCellValue(headers[3]);
        sheet.getRow(0).createCell(4).setCellValue(headers[4]);

        // Populate data rows
        int rowNum = 1;
        for (Module module : modules) {
            sheet.createRow(rowNum).createCell(0).setCellValue(module.getId());
            sheet.getRow(rowNum).createCell(1).setCellValue(module.getName());
            sheet.getRow(rowNum).createCell(2).setCellValue(module.getUrl());
            sheet.getRow(rowNum).createCell(3).setCellValue(module.getIcon());
            sheet.getRow(rowNum).createCell(4).setCellValue(module.getModuleGroup().getName());
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
