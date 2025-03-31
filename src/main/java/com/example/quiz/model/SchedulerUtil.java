package com.example.quiz.model;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SchedulerUtil {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerUtil.class);
    private static Scheduler scheduler;

    @PostConstruct
    public static void startScheduler() {
        try {
            if (scheduler == null || !scheduler.isStarted()) {
                scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.start();
                logger.info("Scheduler started successfully.");
            } else {
                logger.info("Scheduler is already running.");
            }
        } catch (Exception e) {
            logger.error("Error starting the scheduler", e);
        }
    }

    public static void shutdownScheduler() {
        try {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                logger.info("Scheduler shut down successfully.");
            } else {
                logger.info("Scheduler is already shut down or not running.");
            }
        } catch (Exception e) {
            logger.error("Error shutting down the scheduler", e);
        }
    }

    public static Scheduler getScheduler() {
        return scheduler;
    }
}