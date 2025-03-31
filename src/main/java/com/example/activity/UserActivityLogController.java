package com.example.activity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserActivityLogController {

    private final UserActivityLogService userActivityLogService;

    public UserActivityLogController(UserActivityLogService userActivityLogService) {
        this.userActivityLogService = userActivityLogService;
    }

    @GetMapping("/activities")
    public String getUserActivityLog(@RequestParam(required = false) String username,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) String searchQuery,
                                     Model model) {

        Page<UserActivityLog> activityLogs;
        if (username != null && !username.isEmpty()) {
            // Filter by username and searchQuery with pagination
            activityLogs = userActivityLogService.getUserActivityLogs(username, page, size);
        } else {
            // Show all activity logs with pagination
            activityLogs = userActivityLogService.getAllUserActivityLogs(page, size);
        }

        model.addAttribute("activityLogs", activityLogs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", activityLogs.getTotalPages());
        model.addAttribute("totalItems", activityLogs.getTotalElements());
        model.addAttribute("username", username);
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("content", "activities/list");

        return "layout";
    }

}
