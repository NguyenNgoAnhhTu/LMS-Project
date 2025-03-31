package com.example.quiz.service;


import com.example.quiz.model.SchedulerUtil;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class ScheduleJob {
    @Autowired
    private Scheduler scheduler;

    public void scheduleQuizOpenJob(Long quizId, LocalDateTime startTime) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(QuizStatusOpenUpdaterJob.class)
                .withIdentity("quizOpenJob-" + quizId.toString(), "quizJobs")
                .usingJobData("quizId", quizId)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("quizOpenTrigger-" + quizId.toString(), "quizTriggers")
                .startAt(Date.from(startTime.atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void scheduleQuizCloseJob(Long quizId, LocalDateTime closeTime) throws SchedulerException {
        String jobKey = "quizCloseJob-" + quizId;
        JobKey existingJobKey = JobKey.jobKey(jobKey, "quizJobs");

        Scheduler scheduler = SchedulerUtil.getScheduler();

        // Xóa job cũ nếu tồn tại trước khi lên lịch job mới
        if (scheduler.checkExists(existingJobKey)) {
            scheduler.deleteJob(existingJobKey);
        }

        JobDetail jobDetail = JobBuilder.newJob(QuizStatusCloseUpdaterJob.class)
                .withIdentity(jobKey, "quizJobs")
                .usingJobData("quizId", quizId)
                .build();

        // Chỉ chạy job tại thời điểm endTime
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("quizCloseTrigger-" + quizId, "quizTriggers")
                .startAt(Date.from(closeTime.atZone(ZoneId.systemDefault()).toInstant())) // Lên lịch tại endTime
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }


    public void scheduleClearCacheJob(Long quizId, LocalDateTime clearTime) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("quizCacheClearJob-" + quizId, "quizJobs");

        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }

        JobDetail jobDetail = JobBuilder.newJob(QuizCacheClearJob.class)
                .withIdentity(jobKey)
                .usingJobData("quizId", quizId)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("quizCacheClearTrigger-" + quizId, "quizTriggers")
                .startAt(Date.from(clearTime.atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

}