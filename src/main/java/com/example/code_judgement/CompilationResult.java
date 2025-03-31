package com.example.code_judgement;

import lombok.*;

import java.io.File;
import java.nio.file.Path;

@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
public class CompilationResult {
    private boolean success;   // Trạng thái biên dịch thành công hay không
    private String errorMessage;  // Thông báo lỗi nếu biên dịch thất bại
    private String className;  // Tên lớp đã biên dịch (nếu thành công)
    private String randomClassName;
    public CompilationResult(boolean success, String className, String randomClassName) {
        this.success = success;
        this.className = className;
        this.randomClassName = randomClassName;
        this.errorMessage = null; // Không có lỗi
    }

    // Constructor cho kết quả thất bại
    public CompilationResult(boolean success, String errorMessage, String className, String randomClassName) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.className = className;
        this.randomClassName = randomClassName;
    }
}
