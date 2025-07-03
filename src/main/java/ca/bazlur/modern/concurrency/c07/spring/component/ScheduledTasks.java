package ca.bazlur.modern.concurrency.c07.spring.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
public class ScheduledTasks {
    private static final Logger LOGGER
            = LoggerFactory.getLogger(ScheduledTasks.class.getName());

    @Scheduled(fixedRate = 1000)
    public void scheduledTask() {
        LOGGER.info("Scheduled task running on:  {}", Thread.currentThread());
    }
}
