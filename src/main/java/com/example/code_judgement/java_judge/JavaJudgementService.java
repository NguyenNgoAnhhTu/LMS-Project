package com.example.code_judgement.java_judge;

import com.example.code_judgement.CompilationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.concurrent.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JavaJudgementService {

    // Directory to store dynamically created source and class files
    @Value("${BASE_DIR}")
    private String BASE_DIR;
    /**
     * Execute the user's Java code with the provided input and return the output.
     *
     * @param userCode The Java code submitted by the user.
     * @param input    The input provided to the program.
     * @return The output of the program or an error message if something went wrong.
     */
//    public String executeCode(String userCode, String input) {
//        // Ensure the BASE_DIR exists
//        File directory = new File(BASE_DIR);
//        if (!directory.exists()) {
//            directory.mkdirs();
//        }
//
//        // Extract the public class name from the user's code
//        String originalClassName = extractClassName(userCode);
//        if (originalClassName == null) {
//            return "Error: No public class found in Java code!";
//        }
//
//        // Generate a unique class name to avoid conflicts (e.g., MyClass_A1B2C3D4E5F6G7H8)
//        String randomId = generateRandomString(16);
//        String randomClassName = originalClassName + "_" + randomId;
//        String fileName = BASE_DIR + randomClassName + ".java";
//        File sourceFile = new File(fileName);
//        File classFile = new File(BASE_DIR + randomClassName + ".class");
//
//        try {
//            // Replace the original class name with the unique class name in the user's code
//            userCode = userCode.replaceFirst("public class\\s+" + originalClassName,
//                    "public class " + randomClassName);
//
//            // Write the user's code to the source file
//            try (FileWriter writer = new FileWriter(sourceFile)) {
//                writer.write(userCode);
//            }
//
//            // Compile the Java code using javac
//            ProcessBuilder compileBuilder = new ProcessBuilder("javac", fileName);
//            compileBuilder.redirectErrorStream(true);
//            Process compileProcess = compileBuilder.start();
//
//            // Read the compilation output (for debugging if needed)
//            String compileOutput = readProcessOutput(compileProcess.getInputStream());
//            compileProcess.waitFor();
//
//            if (!classFile.exists()) {
//                return "Compilation failed! Check your Java code.\n" + compileOutput;
//            }
//
//            // Run the compiled Java program
//            ProcessBuilder runBuilder = new ProcessBuilder("java", "-cp", BASE_DIR, randomClassName);
//            runBuilder.redirectErrorStream(true);
//            Process runProcess = runBuilder.start();
//
//            // If input is provided, pass it to the program
//            if (input != null) {
//                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
//                    writer.write(input);
//                    writer.flush();
//                }
//            }
//
//            // Capture the program's output
//            String programOutput;
//            boolean runFinished = runProcess.waitFor(2, TimeUnit.SECONDS);
//            if (runFinished) {
//                programOutput = readProcessOutput(runProcess.getInputStream());
//            } else {
//                runProcess.destroyForcibly();
//                return "Error: TLE";
//            }
//
//            return programOutput;
//
//        } catch (Exception e) {
//            return "Error executing code: " + e.getMessage();
//        } finally {
//            // Cleanup: delete the source and class files
//            try {
//                Files.deleteIfExists(sourceFile.toPath());
//                Files.deleteIfExists(classFile.toPath());
//            } catch (IOException ignored) {
//            }
//        }
//    }


    public CompilationResult compileCode(String userCode) {
        // Ensure the BASE_DIR exists
        File directory = new File(BASE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        // Extract the public class name from the user's code
        String originalClassName = extractClassName(userCode);
        if (originalClassName == null) {
            return new CompilationResult(false, "Error: No public class found in Java code!", "Null", "Null");
        }

        // Generate a unique class name to avoid conflicts (e.g., MyClass_A1B2C3D4E5F6G7H8)
        String randomId = generateRandomString(16);
        String randomClassName = originalClassName + "_" + randomId;
        String fileName = BASE_DIR + randomClassName + ".java";
        File sourceFile = new File(fileName);
        File classFile = new File(BASE_DIR + randomClassName + ".class");

        try {
            // Replace the original class name with the unique class name in the user's code
            userCode = userCode.replaceFirst("public class\\s+" + originalClassName,
                    "public class " + randomClassName);

            // Write the user's code to the source file
            try (FileWriter writer = new FileWriter(sourceFile)) {
                writer.write(userCode);
            }

            // Dùng ProcessBuilder để gọi javac
            ProcessBuilder compileBuilder = new ProcessBuilder("javac", fileName);
            compileBuilder.redirectErrorStream(true); // Kết nối stdin và stderr vào cùng một nơi
            Process compileProcess = compileBuilder.start();

            // Đọc thông báo biên dịch từ javac
            String compileOutput = readProcessOutput(compileProcess.getInputStream());
            compileProcess.waitFor(); // Đợi quá trình biên dịch hoàn thành

            // Kiểm tra file .class đã được tạo (biên dịch thành công hay thất bại)
            if (classFile.exists()) {
                return new CompilationResult(true, originalClassName, randomClassName); // Thành công
            } else {
                return new CompilationResult(false, compileOutput, originalClassName, randomClassName); // Thất bại (kèm thông báo lỗi)
            }
        } catch (Exception e) {
            return new CompilationResult(false, "Exception occurred: " + e.getMessage(), originalClassName, randomClassName);
        } finally {
            // Xóa file mã nguồn sau khi biên dịch xong để đảm bảo không rác thải
            try {
                Files.deleteIfExists(sourceFile.toPath());
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Chạy chương trình với `className` đã biên dịch và trả về đầu ra.
     *
     * @param className Tên lớp đã biên dịch.
     * @param input     Đầu vào cho chương trình.
     * @return Đầu ra (output) hoặc thông báo lỗi.
     */
//    public String runCode(String className, String input) {
//        try {
//            // Tạo ProcessBuilder để gọi chương trình Java đã biên dịch
//            ProcessBuilder runBuilder = new ProcessBuilder("java", "-cp", BASE_DIR, className);
//            runBuilder.redirectErrorStream(true); // Kết hợp stdin và stderr
//            Process runProcess = runBuilder.start();
//
//            // Gửi input vào chương trình (nếu cần)
//            if (input != null && !input.isEmpty()) {
//                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
//                    writer.write(input);
//                    writer.flush();
//                }
//            }
//
//            // Đọc output từ chương trình
//            String programOutput = readProcessOutput(runProcess.getInputStream());
//            boolean isFinished = runProcess.waitFor(2, TimeUnit.SECONDS); // Timeout 2 giây (nếu cần thì tăng thêm)
//
//            if (isFinished) {
//                return programOutput.trim(); // Trả về output nếu chương trình chạy xong
//            } else {
//                runProcess.destroyForcibly(); // Hủy tiến trình nếu bị Timeout
//                return "Error: Time Limit Exceeded.";
//            }
//        } catch (Exception e) {
//            return "Error: " + e.getMessage();
//        }
//    }

    public String runCode(String className, String input) {
        ProcessBuilder runBuilder = new ProcessBuilder("java", "-cp", BASE_DIR, className);
        runBuilder.redirectErrorStream(true); // Kết hợp stdout và stderr
        Process runProcess = null;
        try {
            runProcess = runBuilder.start();

            // Gửi input vào chương trình (nếu cần)
            if (input != null && !input.isEmpty()) {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
                    writer.write(input);
                    writer.flush();
                } catch (IOException e) {
                    // Xử lý lỗi khi ghi input
                    return "Error writing input: " + e.getMessage();
                }
            }

            // Đọc output trong một thread riêng để tránh deadlock
            StreamGobbler outputGobbler = new StreamGobbler(runProcess.getInputStream());
            Thread outputThread = new Thread(outputGobbler);
            outputThread.start();

            // Chờ tiến trình hoàn thành với thời gian giới hạn
            boolean finished = runProcess.waitFor(1, TimeUnit.SECONDS);
            if (!finished) {
                runProcess.destroyForcibly();
                return "Error: Time Limit Exceeded (TLE)";
            }

            outputThread.join(); // Đảm bảo đã đọc hết output
            String programOutput = outputGobbler.getOutput();

            return programOutput.trim();

        } catch (IOException e) {
            return "Error executing code: " + e.getMessage();
        } catch (InterruptedException e) {
            if (runProcess != null) {
                runProcess.destroyForcibly();
            }
            Thread.currentThread().interrupt();
            return "Error: Execution was interrupted.";
        } finally {
            if (runProcess != null && runProcess.isAlive()) {
                runProcess.destroyForcibly();
            }
        }
    }

    // Class để đọc output của tiến trình
    private static class StreamGobbler implements Runnable {
        private final InputStream inputStream;
        private final StringBuilder output = new StringBuilder();

        public StreamGobbler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            } catch (IOException e) {
                // Xử lý lỗi khi đọc output
            }
        }

        public String getOutput() {
            return output.toString();
        }
    }

    /**
     * Read the output from the process's InputStream.
     *
     * @param inputStream The InputStream of the process.
     * @return The output as a String.
     * @throws IOException If an I/O error occurs.
     */
    private String readProcessOutput(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }

    /**
     * Extract the public class name from the Java code.
     *
     * @param code The Java code.
     * @return The public class name, or null if not found.
     */
    private String extractClassName(String code) {
        Pattern pattern = Pattern.compile("public class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Generate a random string of uppercase letters and digits with the specified length.
     *
     * @param length The desired length of the random string.
     * @return The generated random string.
     */
    private String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
