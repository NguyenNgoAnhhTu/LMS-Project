package com.example.reports.service;


import com.example.course.Course;
import com.example.course.CourseRepository;
import com.example.course.section.SectionRepository;
import com.example.course.tag.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseReportService {

    @Autowired
    private CourseRepository courseRepository;

//    @Autowired
//    private EnrollmentRepository enrollmentRepository;
//
//    @Autowired
//    private UserCourseProgressRepository userCourseProgressRepository;

    @Autowired
    private SectionRepository sessionRepository;

//    @Autowired
//    private CompletionRepository completionRepository;
//
//    @Autowired
//    private CourseMaterialRepository courseMaterialRepository;

    @Autowired
    private TagRepository tagRepository;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }



//    public Map<Course, Map<User, Object>> getCourseCompletionData() {
//        List<Course> courses = courseRepository.findAll();
//        Map<Course, Map<User, Object>> courseProgress = new HashMap<>();
//
//        for (Course course : courses) {
//            List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);
//            if (!enrollments.isEmpty()) {
//                Map<User, Object> userProgressMap = new HashMap<>();
//                for (Enrollment enrollment : enrollments) {
//                    User user = enrollment.getStudent();
//                    double progressPercent = course.getCompletionPercent(user);
//                    UserCourseProgress lastProgress = userCourseProgressRepository.findLastProgress(user, course);
//
//                    if (lastProgress != null) {
//                        double lastProgressPercent = lastProgress.getProgressPercentage();
//                        double progressChange = progressPercent - lastProgressPercent;
//
//                        if (progressChange != 0) {
//                            lastProgress.setProgressPercentage(progressPercent);
//                            lastProgress.setLastAccessed(System.currentTimeMillis());
//                            userCourseProgressRepository.save(lastProgress);
//                        }
//
//                        userProgressMap.put(user, Map.of(
//                                "progress", progressPercent,
//                                "enrolledAt", enrollment.getDateEnrolled(),
//                                "progressChange", progressChange,
//                                "lastChangedAt", lastProgress.getLastAccessed()
//                        ));
//                    } else {
//                        userCourseProgressRepository.save(new UserCourseProgress(user, course, progressPercent, System.currentTimeMillis()));
//                        userProgressMap.put(user, Map.of(
//                                "progress", progressPercent,
//                                "enrolledAt", enrollment.getDateEnrolled(),
//                                "progressChange", 0,
//                                "lastChangedAt", null
//                        ));
//                    }
//                }
//                courseProgress.put(course, userProgressMap);
//            }
//        }
//        return courseProgress;
//    }

//    public List<Session> getAllSessions() {
//        return sessionRepository.findAll();
//    }
//
//    public List<Completion> getMaterialUsageData() {
//        return completionRepository.findAll();
//    }
//
//    public List<Map<String, Object>> getMaterialTypeDistribution() {
//        return courseMaterialRepository.getMaterialTypeDistribution();
//    }




//    public List<Map<String, Object>> getInstructorPerformanceData() {
//        return courseRepository.getInstructorPerformanceData();
//    }
}
