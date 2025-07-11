package ca.bazlur.modern.concurrency.c05.model;

import ca.bazlur.modern.concurrency.c05.enumeration.Priority;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public record JobContext(String jobName, Priority priority, Map<String, Object> metadata) {
    public JobContext(String jobName, Priority priority) {
        this(jobName, priority, new HashMap<>());
        metadata.put("jobName", jobName);
        metadata.put("priority", priority);
        metadata.put("creationTime", Instant.now());
    }

    public Object getMetadataValue(String key) {
        return metadata.get(key);
    }
}
