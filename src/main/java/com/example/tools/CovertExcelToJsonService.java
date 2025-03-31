package com.example.tools;


import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class CovertExcelToJsonService {
    public Map<String, Object> convertExcelFilesToJson(List<MultipartFile> files) throws IOException {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> errorMap = new HashMap<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                errorMap.put(file.getOriginalFilename(), "The uploaded file is empty.");
                continue; // Bỏ qua file này và tiếp tục với file khác
            }

            try (InputStream inputStream = file.getInputStream();
                 Workbook workbook = new XSSFWorkbook(inputStream)) {

                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    HashMap<String, Object> sheetData = new HashMap<>();

                    try {
                        List<Object> curSheetData = processSheet(sheet);
                        sheetData.put("questions", curSheetData);
                        sheetData.put("total_questions", curSheetData.size());

                        result.put(file.getOriginalFilename() + " - " + sheet.getSheetName(), sheetData);
                    } catch (Exception sheetException) {
                        errorMap.put(file.getOriginalFilename() + " - " + sheet.getSheetName(),
                                "Error processing sheet: " + sheetException.getMessage());
                    }
                }
            } catch (Exception e) {
                errorMap.put(file.getOriginalFilename(), "Error reading file: " + e.getMessage());
            }
        }

        if(!errorMap.isEmpty()) result.put("errors", errorMap);

        return result;
    }

    /*private List<Object> processSheet(Sheet sheet) {
        List<Object> mcQuestions = new ArrayList<>();
        int questionTypeIndex = -1;
        int questionIndex = -1;
        int correctAnswerIndex = -1; // Answer
        int explanationIndex = -1; // Thêm explanation
        Map<String, Integer> answerIndexes = new HashMap<>(); // Chứa <A, cột 1> <B, cột 2>, ....

        Row headerRow = sheet.getRow(0); // check row đầu (header)
        if (headerRow == null) throw new RuntimeException("Sheet " + sheet.getSheetName() + " is empty!");

        for (Cell cell : headerRow) { // check từng cell của row đầu (header)
            String header = getAndFormateStringFromCell(cell).trim().toLowerCase(); // Lấy tên header dưới dạng String và check

            if (header.equals("type")) {
                questionTypeIndex = cell.getColumnIndex();
            } else if (header.equals("question")) {
                questionIndex = cell.getColumnIndex();
            } else if (header.equals("correct")) {
                correctAnswerIndex = cell.getColumnIndex();
            } else if (header.equals("explanation")) {
                explanationIndex = cell.getColumnIndex();
            } else if (header.startsWith("answer option ")) {
                String option = header.replace("answer option ", "").trim();
                answerIndexes.put(option.toUpperCase(), cell.getColumnIndex()); // <A, 1> <B, 2> <C, 3>,...
            }
        }

        if (questionIndex == -1) throw new RuntimeException("No question column found in sheet: " + sheet.getSheetName());
        if (answerIndexes.isEmpty()) throw new RuntimeException("No answer options found in sheet: " + sheet.getSheetName());

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) { // check từng row (Question)
            // QUESTION------------------------------------------
            try {
                Row currentRow = sheet.getRow(rowIndex);
                if (currentRow == null) continue;

                String question = getAndFormateStringFromCell(currentRow.getCell(questionIndex)).trim(); // Lấy question text
                if (question.isEmpty()) continue;

                String questionType = questionTypeIndex != -1
                        ? getAndFormateStringFromCell(currentRow.getCell(questionTypeIndex))
                        : "Multiple Choice";  // Lấy question type

                String correctAnswerList = correctAnswerIndex != -1
                        ? getAndFormateStringFromCell(currentRow.getCell(correctAnswerIndex))
                        : "";    // Lấy answer (A; A, B, C; ....)

                String explanation = explanationIndex != -1
                        ? getAndFormateStringFromCell(currentRow.getCell(explanationIndex))
                        : ""; // Lấy explanation

                Set<Integer> correctIndexes = new HashSet<>();
                if (!correctAnswerList.isEmpty()) { // Lấy answer option
                    for (String correctAnswer : correctAnswerList.split(", ")) {
                        correctAnswer = correctAnswer.trim().toUpperCase();
                        if (!correctAnswer.isEmpty() && correctAnswer.length() == 1) {
                            correctIndexes.add(correctAnswer.charAt(0) - 'A');
                        }
                    }
                }

                List<Map<String, Object>> answers = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : answerIndexes.entrySet()) {
                    int columnIndex = entry.getValue();
                    String answerText = getAndFormateStringFromCell(currentRow.getCell(columnIndex));

                    if (!answerText.isEmpty()) {
                        Map<String, Object> answerObj = new HashMap<>();
                        answerObj.put("text", answerText);

                        String optionKey = entry.getKey(); // "A", "B", "C", ...
                        int optionIndex = optionKey.charAt(0) - 'A';
                        answerObj.put("correct", correctIndexes.contains(optionIndex)); // check đúng sai các câu A, B, C, D,...

                        answers.add(answerObj); // 1 answerObj chứa text: "2"
                    }                           //                  correct: true/false
                }                                                // => Yêu cầu cùng 1 hàng

                Map<String, Object> questionData = new HashMap<>();
                questionData.put("questionText", question);
                questionData.put("questionType", questionType);
                questionData.put("answers", answers);
                questionData.put("explanation", explanation);

                mcQuestions.add(questionData);
            } catch (Exception e) {
                throw new RuntimeException("Error processing row " + rowIndex + ": " + e.getMessage());
            }
        }

        return mcQuestions;
    }*/


    private String getAndFormateStringFromCell(Cell cell) {
        String cellStrValue = getCellValueAsString(cell);
        String formatCellValue = StringEscapeUtils.escapeHtml4(cellStrValue);

        // Convert new line into <br>
        formatCellValue = formatCellValue.replace("\\r", "").replaceAll("\\r?\\n", "<br>");
        formatCellValue = formatCellValue.replace("&lt;br&gt;", "<br>");

        formatCellValue = formatCellValue.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp");

        formatCellValue = formatCellValue.replace("    ", "&nbsp;&nbsp;&nbsp;&nbsp;");

        return formatCellValue;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        DataFormatter formatter = new DataFormatter();

        switch (cell.getCellType()) {
            case STRING:
                return formatter.formatCellValue(cell);
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return cell.toString().trim();
        }
    }


    private List<Object> processSheet(Sheet sheet) {
        List<Object> mcQuestions = new ArrayList<>();
        int questionTypeIndex = -1;
        int questionIndex = -1;
        int correctAnswerIndex = -1; // Answer
        Map<String, Integer> answerIndexes = new HashMap<>(); // Chứa <A, cột 1> <B, cột 2>, ....

        Row headerRow = sheet.getRow(0); // check row đầu (header)
        if (headerRow == null) throw new RuntimeException("Sheet " + sheet.getSheetName() + " is empty!");

        for (Cell cell : headerRow) { // check từng cell của row đầu (header)
            String header = getAndFormateStringFromCell(cell).trim().toLowerCase(); // Lấy tên header dưới dạng String và check

            if (header.equals("type")) {
                questionTypeIndex = cell.getColumnIndex();
            } else if (header.equals("question")) {
                questionIndex = cell.getColumnIndex();
            } else if (header.equals("correct") || header.equals("answer")) {
                correctAnswerIndex = cell.getColumnIndex();
            } else if (header.startsWith("answer option ")) {
                String option = header.replace("answer option ", "").trim();
                answerIndexes.put(option.toUpperCase(), cell.getColumnIndex()); // <A, 1> <B, 2> <C, 3>,...
            }
        }

        if (questionIndex == -1) throw new RuntimeException("No question column found in sheet: " + sheet.getSheetName());
        if (answerIndexes.isEmpty()) throw new RuntimeException("No answer options found in sheet: " + sheet.getSheetName());

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) { // check từng row (Question)
            // QUESTION------------------------------------------
            try {
                Row currentRow = sheet.getRow(rowIndex);
                if (currentRow == null) continue;

                String question = getAndFormateStringFromCell(currentRow.getCell(questionIndex)).trim(); // Lấy question text
                if (question.isEmpty()) continue;

                String questionType = questionTypeIndex != -1
                        ? getAndFormateStringFromCell(currentRow.getCell(questionTypeIndex))
                        : "Multiple Choice";  // Lấy question type
                if (questionType.equalsIgnoreCase("Multiple Choice")){
                    questionType = "MCQ";
                } else if (questionType.equalsIgnoreCase("Single Choice")) {
                    questionType = "SCQ";
                } else {
                    questionType = "TEXT";
                }

                if (questionType.equalsIgnoreCase("Multiple Choice")){
                    questionType = "MCQ";
                } else if (questionType.equalsIgnoreCase("Single Choice")) {
                    questionType = "SCQ";
                } else {
                    questionType = "TEXT";
                }

                String correctAnswerList = correctAnswerIndex != -1
                        ? getAndFormateStringFromCell(currentRow.getCell(correctAnswerIndex))
                        : "";    // Lấy answer (A; A, B, C; ....)

                Set<Integer> correctIndexes = new HashSet<>();
                if (!correctAnswerList.isEmpty()) { // Lấy answer option
                    for (String correctAnswer : correctAnswerList.split(", ")) {
                        correctAnswer = correctAnswer.trim().toUpperCase();
                        if (!correctAnswer.isEmpty() && correctAnswer.length() == 1) {
                            correctIndexes.add(correctAnswer.charAt(0) - 'A');
                        }
                    }
                }

                List<Map<String, Object>> answers = new ArrayList<>(); // Tạo answer data json
                for (Map.Entry<String, Integer> entry : answerIndexes.entrySet()) {
                    int columnIndex = entry.getValue();
                    String answerText = getAndFormateStringFromCell(currentRow.getCell(columnIndex));

                    if (!answerText.isEmpty()) {
                        Map<String, Object> answerObj = new HashMap<>();

                        answerObj.put("optionText", answerText);

                        String optionKey = entry.getKey(); // "A", "B", "C", ...
                        int optionIndex = optionKey.charAt(0) - 'A';

                        answerObj.put("isCorrect", correctIndexes.contains(optionIndex)); // check đúng sai các câu A, B, C, D,...

                        answers.add(answerObj); // 1 answerObj chứa text: "2"
                    }                           //                  correct: true/false
                }

                Map<String, Object> questionData = new LinkedHashMap<>();
                questionData.put("questionText", question);
                questionData.put("questionType", questionType);
                questionData.put("answerOptions", answers);
                mcQuestions.add(questionData);
            } catch (Exception e) {
                throw new RuntimeException("Error processing row " + rowIndex + ": " + e.getMessage());
            }
        }

        return mcQuestions;
    }


    //PROCESS SHEET FOR IMPORT EXCEL
    public List<Object> processSheetForImportExcel(Sheet sheet) {
        List<Object> mcQuestions = new ArrayList<>();
        int questionTypeIndex = -1;
        int questionIndex = -1;
        int correctAnswerIndex = -1; // Answer
        Map<String, Integer> answerIndexes = new HashMap<>(); // Chứa <A, cột 1> <B, cột 2>, ....

        Row headerRow = sheet.getRow(0); // check row đầu (header)
        if (headerRow == null) throw new RuntimeException("Sheet " + sheet.getSheetName() + " is empty!");

        for (Cell cell : headerRow) { // check từng cell của row đầu (header)
            String header = getCellValueAsString(cell).trim().toLowerCase(); // Lấy tên header dưới dạng String và check

            if (header.equals("type")) {
                questionTypeIndex = cell.getColumnIndex();
            } else if (header.equals("question")) {
                questionIndex = cell.getColumnIndex();
            } else if (header.equals("correct") || header.equals("answer")) {
                correctAnswerIndex = cell.getColumnIndex();
            } else if (header.startsWith("answer option ")) {
                String option = header.replace("answer option ", "").trim();
                answerIndexes.put(option.toUpperCase(), cell.getColumnIndex()); // <A, 1> <B, 2> <C, 3>,...
            }
        }

        if (questionIndex == -1) throw new RuntimeException("No question column found in sheet: " + sheet.getSheetName());
        if (answerIndexes.isEmpty()) throw new RuntimeException("No answer options found in sheet: " + sheet.getSheetName());

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) { // check từng row (Question)
            // QUESTION------------------------------------------
            try {
                Row currentRow = sheet.getRow(rowIndex);
                if (currentRow == null) continue;

                String question = getCellValueAsString(currentRow.getCell(questionIndex)).trim(); // Lấy question text
                if (question.isEmpty()) continue;

                String questionType = questionTypeIndex != -1
                        ? getCellValueAsString(currentRow.getCell(questionTypeIndex))
                        : "Multiple Choice";  // Lấy question type

                /*if (questionType.equalsIgnoreCase("Multiple Choice")){
                    questionType = "MCQ";
                } else if (questionType.equalsIgnoreCase("Single Choice")) {
                    questionType = "SCQ";
                } else {
                    questionType = "TEXT";
                }*/

                String correctAnswerList = correctAnswerIndex != -1
                        ? getCellValueAsString(currentRow.getCell(correctAnswerIndex))
                        : "";    // Lấy answer (A; A, B, C; ....)

                Set<Integer> correctIndexes = new HashSet<>();
                if (!correctAnswerList.isEmpty()) { // Lấy answer option
                    for (String correctAnswer : correctAnswerList.split(",")) {
                        correctAnswer = correctAnswer.trim().toUpperCase();
                        if (!correctAnswer.isEmpty() && correctAnswer.length() == 1) {
                            correctIndexes.add(correctAnswer.charAt(0) - 'A');
                        }
                    }
                }

                List<Map<String, Object>> answers = new ArrayList<>(); // Tạo answer data json
                for (Map.Entry<String, Integer> entry : answerIndexes.entrySet()) {
                    int columnIndex = entry.getValue();
                    String answerText = getCellValueAsString(currentRow.getCell(columnIndex));

                    if (!answerText.isEmpty()) {
                        Map<String, Object> answerObj = new LinkedHashMap<>();

                        answerObj.put("optionText", answerText);

                        String optionKey = entry.getKey(); // "A", "B", "C", ...
                        int optionIndex = optionKey.charAt(0) - 'A';

                        answerObj.put("isCorrect", correctIndexes.contains(optionIndex)); // check đúng sai các câu A, B, C, D,...

                        answers.add(answerObj); // 1 answerObj chứa text: "2"
                    }                           //                  correct: true/false
                }

                Map<String, Object> questionData = new LinkedHashMap<>();
                questionData.put("questionText", question);
                questionData.put("questionType", questionType);
                questionData.put("answerOptions", answers);
                mcQuestions.add(questionData);
            } catch (Exception e) {
                throw new RuntimeException("Error processing row " + rowIndex + ": " + e.getMessage());
            }
        }

        return mcQuestions;
    }

}
