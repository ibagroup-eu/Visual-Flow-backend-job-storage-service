package eu.ibagroup.vfjobstorageservice.model;

import com.fasterxml.jackson.databind.JsonNode;
import eu.ibagroup.vfjobstorageservice.dto.jobs.JobParams;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serial;
import java.io.Serializable;

/**
 * Job class
 */
@Builder(toBuilder = true)
@RedisHash("Job")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job implements Serializable {
    @Id
    private String id;
    private String name;
    private long runId;
    private transient JsonNode definition;
    private JobParams params;
    private String startedAt;
    private String finishedAt;
    private String lastModified;
    private String status;
    private boolean runnable;
    private boolean editable;
    @Serial
    private static final long serialVersionUID = 2405172041950251807L;
}
