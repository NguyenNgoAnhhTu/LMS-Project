package com.example.course.material;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseMaterialDTO {
    private Long id;
    private String title;
    private String url;

    public static CourseMaterialDTO toDTO(CourseMaterial courseMaterial) {
        return CourseMaterialDTO.builder()
                .id(courseMaterial.getId())
                .title(courseMaterial.getTitle())
                .url(courseMaterial.getMaterialUrl())
                .build();
    }
}
