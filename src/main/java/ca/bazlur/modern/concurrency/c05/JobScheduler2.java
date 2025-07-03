package ca.bazlur.modern.concurrency.c05;

// todo: created new file for ScopedValue
public class JobScheduler2 {
    private static final ScopedValue<JobContext> CONTEXT = ScopedValue.newInstance(); //①

    public static JobContext getContext() { //⑤
        return CONTEXT.get();
    }

    public static Object getJobMetadata(String key) {
        JobContext context = CONTEXT.get(); //⑥
        if (context != null) {
            return context.getMetadataValue(key);
        }
        return null;
    }

    public void schedule(Job job, String jobName, Priority priority) {
        JobContext context = new JobContext(jobName, priority); //②

        ScopedValue.where(CONTEXT, context)
                .run(() -> runJob(job)); //③
    }

    private void runJob(Job job) {
        job.execute(); //④
    }
}
