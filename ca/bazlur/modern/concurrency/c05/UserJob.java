package ca.bazlur.modern.concurrency.c05;

public class UserJob implements Job {

    private final JobScheduler jobScheduler;

    public UserJob(JobScheduler jobScheduler) {
        this.jobScheduler = jobScheduler;
    }

    @Override
    public void execute() { // ⑥
        System.out.println("User job is running!");

        // No context parameter needed - framework handles it internally
        Object creationTime = jobScheduler.getJobMetadata("creationTime"); // ⑦
        System.out.println("Job creation time: " + creationTime);

        // Helper methods are now clean
        processJobData();
    }

    private void processJobData() { // ⑧
        // Clean method signature - no framework parameters
        Object priority = jobScheduler.getJobMetadata("priority");
        System.out.println("Processing job with priority: " + priority);
    }

// ---------------------
//    @Override
//    public void execute(JobContext context) { // ④
//        System.out.println("User job is running!");
//
//        // User code calls back into the framework to retrieve metadata
//        Object creationTime = jobScheduler.getJobMetadata("creationTime", context); // ⑤
//        System.out.println("Job creation time: " + creationTime);
//
//        // Any helper methods also need the context parameter
//        processJobData(context); // ⑥
//    }
//
//    private void processJobData(JobContext context) { // ⑦
//        // Even though this method might not directly use context,
//        // it needs the parameter to pass to other framework methods
//        Object priority = jobScheduler.getJobMetadata("priority", context);
//        System.out.println("Processing job with priority: " + priority);
//    }
}
