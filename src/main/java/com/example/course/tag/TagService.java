package com.example.course.tag;

import com.example.course.topic.TopicRepository;
import com.example.course.topic.Topic;
import com.example.utils.Helper;
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

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TopicRepository topicRepository;

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Tag getTagById(Integer id) {
        return tagRepository.findById(id).orElse(null);
    }

    public Tag createTag(String tagName, Topic topic) {
        Tag tag = new Tag();
        tag.setTagName(tagName);
        tag.setTopic(topic);
        return tagRepository.save(tag);
    }

    public void updateTag(Long id, String tagName, Topic topic) {
        Tag tag = tagRepository.findById(Math.toIntExact(id)).orElseThrow(() -> new RuntimeException("Tag not found"));
        tag.setTagName(tagName);
        tag.setTopic(topic); // Update the associated Topic
        tagRepository.save(tag);
    }

    public void deleteTag(Integer id) {
        tagRepository.deleteById(id);
    }

    public void saveAll(List<Tag> tags) {
        tagRepository.saveAll(tags);  // Save all tags at once
    }
    // Method to fetch paginated tags
    public Page<Tag> getTags(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return tagRepository.findAll(pageable);
    }
    public Page<Tag> searchTags(String searchQuery, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return tagRepository.findByTagNameContainingIgnoreCase(searchQuery, pageable);
    }

    public List<Tag> importExcel(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            List<Tag> tags = new ArrayList<>();

            for (int i = 1; i < rowCount; i++) { // Start from 1 to skip the header row
                Row row = sheet.getRow(i);
                if (row != null) {
                    // Use helper function to get values
                    String tagName = Helper.getCellValueAsString(row.getCell(0)).trim(); // Assume tag name is in column 0
                    String topicName = Helper.getCellValueAsString(row.getCell(1)).trim(); // Assume topic name is in column 1

                    // Find or create the Topic
                    Topic topic = topicRepository.findByTopicName(topicName)
                            .orElseGet(() -> {
                                Topic newTopic = new Topic();
                                newTopic.setTopicName(topicName);
                                return topicRepository.save(newTopic); // Save the new Topic
                            });

                    // Check if the Tag already exists
                    if (!tagExists(tagName, topic)) {
                        Tag tag = new Tag();
                        tag.setTagName(tagName);
                        tag.setTopic(topic); // Associate the Tag with the Topic
                        tags.add(tag);
                    }
                }
            }

            // Save Tags to the database
            return tagRepository.saveAll(tags);
        } catch (IOException e) {
            throw new RuntimeException("Error importing tags from Excel", e);
        }
    }


    // Helper method to check if a tag with the same name and topic exists
    public boolean tagExists(String tagName, Topic topic) {
        return tagRepository.existsByTagNameAndTopic(tagName, topic);
    }


    // Method to export tags to Excel
    public ByteArrayInputStream exportTagsToExcel(List<Tag> tags) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Tags");

        // Create the header row
        String[] headers = { "Tag ID", "Tag Name" };
        sheet.createRow(0).createCell(0).setCellValue(headers[0]);
        sheet.getRow(0).createCell(1).setCellValue(headers[1]);

        // Populate data rows
        int rowNum = 1;
        for (Tag tag : tags) {
            sheet.createRow(rowNum).createCell(0).setCellValue(tag.getTagId());
            sheet.getRow(rowNum).createCell(1).setCellValue(tag.getTagName());
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
