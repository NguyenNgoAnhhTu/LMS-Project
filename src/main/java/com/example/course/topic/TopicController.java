package com.example.course.topic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

@Controller
@RequestMapping("/topics")
public class TopicController {

    @Autowired
    private TopicService topicService;

    // Thêm model attribute chung cho tất cả các phương thức
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("title", "Topic");
        model.addAttribute("links", "/style.css");
    }

    // Get paginated list of topics
    @GetMapping()
    public String getTopics(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(value = "searchQuery", required = false) String searchQuery) {
        Page<Topic> topicsPage;

        if (searchQuery != null && !searchQuery.isEmpty()) {
            // If there is a search query, use it to filter the topics
            topicsPage = topicService.searchTopics(searchQuery, page, size);
        } else {
            // If no search query, just get all topics with pagination
            topicsPage = topicService.getTopics(page, size);
        }

        model.addAttribute("topicsPage", topicsPage);
        model.addAttribute("searchQuery", searchQuery); // Pass search query back to the view

        // add attribute for layout
        model.addAttribute("content","topic/list");

        return "layout";
    }

    // Show create form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("topic", new Topic());
        model.addAttribute("content", "topic/create");
        return "layout";
    }

    // Create new topic
    @PostMapping("/create")
    public String createTopic(@ModelAttribute Topic topic, Model model) {
        if (topicService.isTopicNameExists(topic.getTopicName())) {
            model.addAttribute("error", "Topic name already exists!");
            return "topic/create"; // Ensure this is the correct view name
        }
        topicService.createTopic(topic.getTopicName());
        return "redirect:/topics"; // Ensure this is the correct view name
    }

    // Show edit form for a specific topic
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Topic topic = topicService.getTopicById(id);
        if (topic == null) {
            model.addAttribute("error", "Topic not found!");
            return "redirect:/topics"; // Redirect if topic not found
        }
        model.addAttribute("topic", topic);

        model.addAttribute("content", "topic/edit");
        return "layout";
    }

    // Update existing topic
    @PostMapping("/edit/{id}")
    public String updateTopic(@PathVariable Integer id, @ModelAttribute Topic topic, Model model) {
        if (topicService.isTopicNameExists(topic.getTopicName())) {
            model.addAttribute("error", "Topic name already exists!");
            return "topic/edit"; // Ensure this is the correct view name
        }
        topicService.updateTopic(id, topic.getTopicName());
        return "redirect:/topics";
    }

    // Delete a topic
    @GetMapping("/delete/{id}")
    public String deleteTopic(@PathVariable Integer id, Model model) {
        try {
            topicService.deleteTopic(id);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage()); // Add error message to the model
        }
        return "redirect:/topics";
    }

    // Export topics to Excel
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportTopics() {
        // Fetch all topics (page size set to max to get all records)
        Page<Topic> topicsPage = topicService.getTopics(0, Integer.MAX_VALUE);

        // Convert topics to Excel (assumes you have a service for that)
        ByteArrayInputStream excelFile = topicService.exportTopicsToExcel(topicsPage.getContent());

        // Create headers for the response (Content-Disposition to trigger file download)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=topics.xlsx");

        // Return the file in the response
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(excelFile));
    }

    // Import topics from Excel
    @PostMapping("/import")
    public String importTopics(@RequestParam("file") MultipartFile file, Model model) {
        try {
            List<Topic> topics = topicService.importExcel(file);
            topicService.saveAll(topics);  // Save the topics in the database
        } catch (Exception e) {
            model.addAttribute("error", "Error importing topics: " + e.getMessage());
        }
        return "redirect:/topics";  // Redirect to the topics list page after import
    }
}
