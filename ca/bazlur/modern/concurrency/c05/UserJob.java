package ca.bazlur.modern.concurrency.c05;

// todo: replaced the previous version
public class UserJob implements Job {
    private final JobScheduler jobScheduler;

    public UserJob(JobScheduler jobScheduler) {
        this.jobScheduler = jobScheduler;
    }

    @Override
    public void execute() {
        System.out.println("User job is running!");

        Object creationTime = jobScheduler.getJobMetadata("creationTime");
        System.out.println("Job creation time: " + creationTime);
    }
}
