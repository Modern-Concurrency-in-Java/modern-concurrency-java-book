package ca.bazlur.modern.concurrency.c07.jee;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
@ManagedScheduledExecutorDefinition(
        name = "java:module/concurrent/virtual-scheduler",
        virtual = true // Enables Virtual Threads for Scheduled Tasks
)
public class VirtualThreadSchedulerExample {

    @Resource(lookup = "java:module/concurrent/virtual-scheduler")
    private ManagedScheduledExecutorService scheduledExecutor;

    public void scheduleTask() {
        scheduledExecutor.schedule(() -> {
            System.out.println("Scheduled task running in virtual thread: " + Thread.currentThread());
        }, 5, TimeUnit.SECONDS); // Delay execution by 5 seconds
    }
}
