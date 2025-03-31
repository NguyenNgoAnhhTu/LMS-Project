package com.example.course.topic;

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
public class TopicService {

    @Autowired
    private TopicRepository topicRepository;

    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }

    public Topic getTopicById(Integer id) {
        return topicRepository.findById(id).orElse(null);
    }

    public Topic createTopic(String topicName) {
        Topic topic = new Topic();
        topic.setTopicName(topicName);
        return topicRepository.save(topic);
    }

    public Topic updateTopic(Integer id, String topicName) {
        Optional<Topic> existingTopic = topicRepository.findById(id);
        if (existingTopic.isPresent()) {
            Topic topic = existingTopic.get();
            topic.setTopicName(topicName);
            return topicRepository.save(topic);
        }
        return null;
    }

    public void deleteTopic(Integer id) {
        topicRepository.deleteById(id);
    }

    public void saveAll(List<Topic> topics) {
        topicRepository.saveAll(topics);  // Save all topics at once
    }
    // Method to fetch paginated topics
    public Page<Topic> getTopics(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return topicRepository.findAll(pageable);
    }
    public Page<Topic> searchTopics(String searchQuery, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return topicRepository.findByTopicNameContainingIgnoreCase(searchQuery, pageable);
    }

    // Import topics from an Excel file
    public List<Topic> importExcel(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            List<Topic> topics = new ArrayList<>();

            for (int i = 1; i < rowCount; i++) { // Start from 1 to skip the header row
                Row row = sheet.getRow(i);
                if (row != null) {
                    String topicName = row.getCell(0).getStringCellValue(); // Assume topic name is in column 0

                    // Check if the topic already exists
                    if (!topicExists(topicName)) {
                        Topic topic = new Topic();
                        topic.setTopicName(topicName);
                        topics.add(topic);
                    }
                }
            }

            // Save topics to the database
            return topicRepository.saveAll(topics); // Assuming you have a topicRepository bean
        } catch (IOException e) {
            throw new RuntimeException("Error importing topics from Excel", e);
        }
    }

    private boolean topicExists(String topicName) {
        return topicRepository.findByTopicName(topicName).isPresent(); // Assuming you have this method in TopicRepository
    }

    // Method to export topics to Excel
    public ByteArrayInputStream exportTopicsToExcel(List<Topic> topics) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Topics");

        // Create the header row
        String[] headers = { "Topic ID", "Topic Name" };
        sheet.createRow(0).createCell(0).setCellValue(headers[0]);
        sheet.getRow(0).createCell(1).setCellValue(headers[1]);

        // Populate data rows
        int rowNum = 1;
        for (Topic topic : topics) {
            sheet.createRow(rowNum).createCell(0).setCellValue(topic.getTopicId());
            sheet.getRow(rowNum).createCell(1).setCellValue(topic.getTopicName());
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

    public boolean isTopicNameExists(String topicName) {
        return this.topicExists(topicName);
    }
}
