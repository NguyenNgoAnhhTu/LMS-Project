package com.example.course.material;

import com.example.course.section.Section;
import com.example.course.section.SectionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CourseMaterialService {
    private final CourseMaterialRepository courseMaterialRepository;
    private final SectionRepository sectionRepository;

    //Thư mục lưu trữ file
    @Value("${upload.dir}")
    private String uploadDir;

    public List<CourseMaterial> findAll() {
        return courseMaterialRepository.findAll();
    }

    public List<CourseMaterial> findAllBySectionId(Long sectionId) {
        Section section = sectionRepository.findById(sectionId).orElseThrow(EntityNotFoundException::new);
        return courseMaterialRepository.findBySection(section);
    }

    public CourseMaterialDTO createMaterial(MultipartFile file, Long sectionId, String materialType, String title) throws IOException {
        Section section = sectionRepository.findById(sectionId).orElseThrow(EntityNotFoundException::new);
        // 1. Kiểm tra file
        if (file.isEmpty()) {
            throw new RuntimeException("No file selected for upload.");
        }

        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new RuntimeException("Only PDF files are allowed.");
        }

        // 2. Tạo thư mục upload nếu chưa tồn tại
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 3. Đảm bảo tên file không bị trùng
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        String fileID = System.currentTimeMillis() + ".pdf";
        String url = "http://localhost/material/" + fileID;
        //String url = "/material/" + fileID;
        System.out.println(url);

        // 4. Lưu file
        Path filePath = uploadPath.resolve(fileID);
        file.transferTo(filePath.toFile());
        CourseMaterial courseMaterial = CourseMaterial.builder()
                .materialId(fileID)
                .section(section)
                .materialType(CourseMaterial.MaterialType.valueOf(materialType))
                .materialName(originalFileName)
                .materialUrl(url)
                .title(title)
                .build();
        return CourseMaterialDTO.toDTO(courseMaterialRepository.save(courseMaterial));

    }
}
