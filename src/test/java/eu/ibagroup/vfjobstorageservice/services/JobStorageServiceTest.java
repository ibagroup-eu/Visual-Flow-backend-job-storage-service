package eu.ibagroup.vfjobstorageservice.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ibagroup.vfjobstorageservice.dto.jobs.JobDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class JobStorageServiceTest {
    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private HashOperations hashOperations;

    private JobStorageService jobStorageService;

    @BeforeEach
    void setUp() {
        jobStorageService = new JobStorageService(redisTemplate, new ObjectMapper());
    }

    @Test
    void testCreate() throws JsonProcessingException {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        doNothing().when(hashOperations).put(anyString(), anyString(), any());
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = "{\"graph\":[]}";
        JsonNode rootNode = objectMapper.readTree(jsonString);
        jobStorageService.create("projectId", JobDto.builder().definition(rootNode).build());
        verify(hashOperations).put(eq("project:projectId"), anyString(), any());
    }

    @Test
    void testGet() throws IOException {
        Path file = Path.of("", "src/test/resources").resolve("jobs.json");
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        String jsonJob = Files.readString(file);
        when(hashOperations.get(any(), any())).thenReturn(jsonJob);
        jobStorageService.get("projectId", "jobId");
        verify(hashOperations).get(any(), any());
    }

    @Test
    void testGetAll() throws IOException {
        Path file = Path.of("", "src/test/resources").resolve("jobs.json");
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        String jsonJob = Files.readString(file);
        when(hashOperations.entries(any())).thenReturn(Map.of("project:project3:job:41b95016-d0fd-4d5f-acbf-45764b6694d1", jsonJob, "project:project3:job:468ba0e3-5364-44fa-acbf-f474215715b4", jsonJob));
        jobStorageService.getAll("projectId");
        verify(hashOperations).entries(any());
    }

    @Test
    void testUpdate() throws IOException {
        Path file = Path.of("", "src/test/resources").resolve("jobs.json");
        String jsonJob = Files.readString(file);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get(any(), any())).thenReturn(jsonJob);
        doNothing().when(hashOperations).put(anyString(), anyString(), any());
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = "{\"graph\":[]}";
        JsonNode rootNode = objectMapper.readTree(jsonString);
        jobStorageService.update("projectId", "jobId", JobDto.builder().definition(rootNode).build());
        verify(hashOperations).put(eq("project:projectId"), anyString(), any());
    }

    @Test
    void testUpdateStatus() throws IOException {
        Path file = Path.of("", "src/test/resources").resolve("jobs.json");
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        String jsonJob = Files.readString(file);
        when(hashOperations.get(any(), any())).thenReturn(jsonJob);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        doNothing().when(hashOperations).put(anyString(), anyString(), any());
        jobStorageService.updateStatus("projectId", "jobId", "newStatus", JobDto.builder().build());
        verify(hashOperations).put(eq("project:projectId"), anyString(), any());
    }

    @Test
    void testDelete() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.delete(any(), any())).thenReturn(1L);
        jobStorageService.delete("projectId", "jobId");
        verify(hashOperations).delete(any(), any());
    }

}
