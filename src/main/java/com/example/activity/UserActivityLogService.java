package com.example.activity;

import com.example.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserActivityLogService {

    private final UserActivityLogRepository userActivityLogRepository;

    public UserActivityLogService(UserActivityLogRepository userActivityLogRepository) {
        this.userActivityLogRepository = userActivityLogRepository;
    }

    // Get paginated logs for a specific user
    public Page<UserActivityLog> getUserActivityLogs(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userActivityLogRepository.findByUser_Username(username, pageable);
    }

    // Get all paginated user activity logs
    public Page<UserActivityLog> getAllUserActivityLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userActivityLogRepository.findAll(pageable);
    }

    // Log activity
    public void logActivity(User user, UserActivityLog.ActivityType activityType, String activityDetails) {
        UserActivityLog activityLog = new UserActivityLog(
                user,
                activityType,
                activityDetails,
                LocalDateTime.now()
        );
        userActivityLogRepository.save(activityLog);
    }
}
