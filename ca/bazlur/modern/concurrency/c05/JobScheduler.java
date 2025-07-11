package ca.bazlur.modern.concurrency.c05;

import ca.bazlur.modern.concurrency.c05.enumeration.Priority;
import ca.bazlur.modern.concurrency.c05.model.JobContext;

public class JobScheduler {
    private static final ScopedValue<JobContext> CONTEXT = ScopedValue.newInstance(); //①

    public void schedule(Job job, String jobName, Priority priority) {
        JobContext context = new JobContext(jobName, priority); //②

        ScopedValue.where(CONTEXT, context)
                .run(() -> runJob(job)); //③
    }

    private void runJob(Job job) {
        job.execute(); //④
    }

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

// ---------------------

//    private static final ThreadLocal<JobContext> jobContextHolder = new ThreadLocal<>(); // ①
//
//    public void schedule(Job job, String jobName, Priority priority) {
//        JobContext context = new JobContext(jobName, priority);
//        try {
//            jobContextHolder.set(context); // ②
//            runJob(job);
//        } finally {
//            jobContextHolder.remove(); // ③
//        }
//    }
//
//    private void runJob(Job job) { // ④
//        job.execute();
//    }
//
//    public Object getJobMetadata(String key) {
//        JobContext context = jobContextHolder.get(); // ⑤
//        return (context != null) ? context.getMetadataValue(key) : null;
//    }

//    ---------------------

//    public void schedule(Job job, String jobName, Priority priority) { // ①
//        JobContext context = new JobContext(jobName, priority);
//        runJob(job, context);
//    }
//
//    private void runJob(Job job, JobContext context) { // ②
//        // The framework calls user code here, passing the context
//        job.execute(context);
//    }
//
//    public Object getJobMetadata(String key, JobContext context) { // ③
//        if (context == null) {
//            return null;
//        }
//        return context.getMetadataValue(key);
//    }
}
