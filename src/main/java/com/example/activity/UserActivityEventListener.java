package com.example.activity;

import com.example.user.User;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class UserActivityEventListener {

    private final UserActivityLogService userActivityLogService;

    public UserActivityEventListener(UserActivityLogService userActivityLogService) {
        this.userActivityLogService = userActivityLogService;
    }

    @Component
    public class LoginListener implements ApplicationListener<AuthenticationSuccessEvent> {

        @Override
        public void onApplicationEvent(AuthenticationSuccessEvent event) {
            // Get the user details from the authentication object
            UserDetails userDetails = (UserDetails) event.getAuthentication().getPrincipal();

            // Check if the userDetails is an instance of your custom User class
            if (userDetails instanceof User) {
                User user = (User) userDetails;

                // Log the user activity for login
                userActivityLogService.logActivity(
                        user, // Custom User
                        UserActivityLog.ActivityType.LOGIN,
                        "User logged in"
                );
            } else if (userDetails instanceof org.springframework.security.core.userdetails.User) {
                // Handle case for the default Spring Security User
                org.springframework.security.core.userdetails.User springSecurityUser =
                        (org.springframework.security.core.userdetails.User) userDetails;

                // Handle logic for default User (perhaps log it or ignore it)
                System.out.println("User is of type Spring Security's User: " + springSecurityUser.getUsername());
            } else {
                // If the user type is unexpected, log it
                System.err.println("Unexpected user type: " + userDetails.getClass().getName());
            }
        }
    }
}
