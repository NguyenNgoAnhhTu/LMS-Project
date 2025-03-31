package com.example.course.tag;

import com.example.course.topic.Topic;
import com.example.course.topic.TopicService;
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
@RequestMapping("/tags")
public class TagController {

    @Autowired
    private TagService tagService;
    @Autowired
    private TopicService topicService;

    // Thêm model attribute chung cho tất cả các phương thức
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("title", "Tags");
        model.addAttribute("links", "/style.css");
    }

    // Get paginated list of tags
    @GetMapping()
    public String getTags(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(value = "searchQuery", required = false) String searchQuery) {
        Page<Tag> tagsPage;

        if (searchQuery != null && !searchQuery.isEmpty()) {
            // If there is a search query, use it to filter the tags
            tagsPage = tagService.searchTags(searchQuery, page, size);
        } else {
            // If no search query, just get all tags with pagination
            tagsPage = tagService.getTags(page, size);
        }

        model.addAttribute("tagsPage", tagsPage);
        model.addAttribute("searchQuery", searchQuery); // Pass search query back to the view
        // add attribute for layout
        model.addAttribute("content","tag/list");

        return "layout";
    }

    // Show create form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("tag", new Tag());
        model.addAttribute("topics", topicService.getAllTopics()); // Fetch all topics
        model.addAttribute("content", "tag/create");
        return "layout";
    }

    // Create new tag
    @PostMapping("/create")
    public String createTag(@ModelAttribute Tag tag, Model model) {
        // Get the Topic object from the tag
        Topic topic = tag.getTopic();

        if (tagService.tagExists(tag.getTagName(), topic)) {
            model.addAttribute("error", "Tag name already exists for this topic!");
            return "tag/create"; // Ensure this is the correct view name
        }

        tagService.createTag(tag.getTagName(), topic);
        return "redirect:/tags"; // Ensure this is the correct view name
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Tag tag = tagService.getTagById(Math.toIntExact(id));
        if (tag == null) {
            model.addAttribute("error", "Tag not found!");
            return "error/404";
        }

        model.addAttribute("tag", tag); // Add Tag object
        model.addAttribute("topics", topicService.getAllTopics()); // Add available topics for dropdown
        model.addAttribute("content", "tag/edit");
        return "layout";
    }

    // Update existing tag
    @PostMapping("/edit/{id}")
    public String updateTag(@PathVariable Integer id, @ModelAttribute Tag tag, Model model) {
        // Get the Topic object from the tag
        Topic topic = tag.getTopic();

        if (tagService.tagExists(tag.getTagName(), topic)) {
            model.addAttribute("error", "Tag name already exists for this topic!");
            return "tag/edit";
        }
        tagService.updateTag(Long.valueOf(id), tag.getTagName(), topic);
        return "redirect:/tags";
    }

    // Delete a tag
    @GetMapping("/delete/{id}")
    public String deleteTag(@PathVariable Integer id) {
        tagService.deleteTag(id);
        return "redirect:/tags";
    }

    // Export tags to Excel
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportTags() {
        // Fetch all tags (page size set to max to get all records)
        Page<Tag> tagsPage = tagService.getTags(0, Integer.MAX_VALUE);

        // Convert tags to Excel (assumes you have a service for that)
        ByteArrayInputStream excelFile = tagService.exportTagsToExcel(tagsPage.getContent());

        // Create headers for the response (Content-Disposition to trigger file download)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=tags.xlsx");

        // Return the file in the response
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(excelFile));
    }

    // Import tags from Excel
    @PostMapping("/import")
    public String importTags(@RequestParam("file") MultipartFile file) {
        List<Tag> tags = tagService.importExcel(file);
        tagService.saveAll(tags);  // Save the tags in the database
        return "redirect:/tags";  // Redirect to the tags  list page after import
    }
}
