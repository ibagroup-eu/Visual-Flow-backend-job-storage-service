package eu.ibagroup.vfjobstorageservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ibagroup.vfjobstorageservice.dto.connections.ConnectionDto;
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
public class ConnectionServiceTest {
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private HashOperations hashOperations;
    private ConnectionService connectionService;
    private static final String PROJECT_ID = "vf-project-name";

    @BeforeEach
    void setUp() {
        connectionService = new ConnectionService(redisTemplate, new ObjectMapper());
    }

    @Test
    void testCreate() throws JsonProcessingException {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        doNothing().when(hashOperations).put(anyString(), anyString(), any());
        connectionService.create(PROJECT_ID, ConnectionDto.builder().build());
        verify(hashOperations).put(eq("connection:vf-project-name"), anyString(), any());
    }

    @Test
    void testUpdate() throws JsonProcessingException {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        doNothing().when(hashOperations).put(anyString(), anyString(), any());
        connectionService.update(PROJECT_ID, ConnectionDto.builder().build());
        verify(hashOperations).put(eq("connection:vf-project-name"), anyString(), any());
    }

    @Test
    void testGetAll() throws IOException {
        Path file = Path.of("", "src/test/resources").resolve("connections.json");
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        String jsonConnection = Files.readString(file);
        when(hashOperations.entries(any())).thenReturn(Map.of("connection:vf-project-name:connectionId", jsonConnection, "connection:vf-project-name:connectionId1", jsonConnection));
        connectionService.getAll(PROJECT_ID);
        verify(hashOperations).entries(any());
    }

    @Test
    void testGet() throws IOException {
        Path file = Path.of("", "src/test/resources").resolve("connections.json");
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        String jsonConnection = Files.readString(file);
        when(hashOperations.get("connection:vf-project-name", "51441ddd-53cd-41f4-9dde-371de6e315d6")).thenReturn(jsonConnection);
        connectionService.get(PROJECT_ID, "51441ddd-53cd-41f4-9dde-371de6e315d6");
        verify(hashOperations).get("connection:vf-project-name", "51441ddd-53cd-41f4-9dde-371de6e315d6");
    }

    @Test
    void testDelete() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.delete(any(), any())).thenReturn(1L);
        connectionService.delete(PROJECT_ID, "key");
        verify(hashOperations).delete(any(), any());
    }

    @Test
    void testDeleteAll() throws IOException {
        Path file = Path.of("", "src/test/resources").resolve("connections.json");
        String jsonConnection = Files.readString(file);
        when(hashOperations.entries(any())).thenReturn(Map.of("connection:vf-project-name:connectionId", jsonConnection, "connection:vf-project-name:connectionId1", jsonConnection));
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.delete(any(), any())).thenReturn(1L);
        connectionService.deleteAll(PROJECT_ID);
        verify(hashOperations, times(2)).delete(any(), any());
    }

}
