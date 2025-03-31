package com.example.course.material;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

//@Controller
//@RequestMapping("/materials")
//public class FileController {
//    @Value("${upload.dir}")
//    private String uploadDir;
//
//    @PostMapping("/upload")
//    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
//        if (file.isEmpty()) {
//            model.addAttribute("error", "Please select a file to upload.");
//            return "redirect:/courses/material";
//        }
//
//        // Kiểm tra định dạng file
//        if (!Objects.requireNonNull(file.getContentType()).equalsIgnoreCase("application/pdf")) {
//            model.addAttribute("error", "Only PDF files are allowed.");
//            return "redirect:/courses/material";
//        }
//
//        try {
//            // Kiểm tra thư mục upload và tạo thư mục nếu cần
//            Path uploadPath = Paths.get(uploadDir);
//            if (!Files.exists(uploadPath)) {
//                Files.createDirectories(uploadPath);
//            }
//
//            // Lưu file vào thư mục
//            String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
//            Path filePath = uploadPath.resolve(originalFilename);
//            file.transferTo(filePath.toFile());
//
//            model.addAttribute("success", "File uploaded successfully: " + originalFilename);
//        } catch (IOException e) {
//            model.addAttribute("error", "File upload failed: " + e.getMessage());
//        }
//
//        return "redirect:/courses/material";
//    }
//
//}

@RestController
@RequestMapping("/materials")
public class FileController {
    private final CourseMaterialService courseMaterialService;


    public FileController(CourseMaterialService courseMaterialService) {
        this.courseMaterialService = courseMaterialService;
    }

    // Xử lý upload file
    @PostMapping("/upload")
    public ResponseEntity<?> uploadMaterial(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sectionId") Long sectionId,
            @RequestParam("materialType") String materialType,
            @RequestParam("title") String title) {
        try {
            return ResponseEntity.ok(courseMaterialService.createMaterial(file,sectionId, materialType,title));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
