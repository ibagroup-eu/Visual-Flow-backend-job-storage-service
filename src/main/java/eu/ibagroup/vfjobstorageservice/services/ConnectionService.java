/*
 * Copyright (c) 2021 IBA Group, a.s. All rights reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.ibagroup.vfjobstorageservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ibagroup.vfjobstorageservice.dto.connections.ConnectionDto;
import eu.ibagroup.vfjobstorageservice.dto.connections.ConnectionOverviewDto;
import eu.ibagroup.vfjobstorageservice.model.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static eu.ibagroup.vfjobstorageservice.dto.Constants.PROJECT_CONNECTION_PREFIX;

@Slf4j
@Service
public class ConnectionService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    public ConnectionService(@Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate,
                            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public String create(String projectId, ConnectionDto connectionDto) throws JsonProcessingException {
        String folderKey = PROJECT_CONNECTION_PREFIX + projectId;
        String connectionKey = connectionDto.getKey();
        if (connectionKey == null) {
            connectionKey = java.util.UUID.randomUUID().toString();
        }
        Connection connection = Connection.builder()
                .key(connectionKey)
                .value(connectionDto.getValue())
                .build();
        String connectionJson = objectMapper.writeValueAsString(connection);
        redisTemplate.opsForHash().put(folderKey, connectionKey, connectionJson);
        return connectionKey;

    }

    public void update(String projectId, ConnectionDto connectionDto)
            throws JsonProcessingException {
        create(projectId, connectionDto);
    }


    public ConnectionOverviewDto getAll(String projectId) {
        List<Connection> connections = new ArrayList<>();
        String folderKey = PROJECT_CONNECTION_PREFIX + projectId;
        redisTemplate.opsForHash().entries(folderKey).forEach((Object key, Object value) -> {
            try {
                connections.add(jsonToConnection((String) value));
            } catch (JsonProcessingException e) {
                LOGGER.error("Error while executing getAll method: " + e.getMessage());
            }
        });
        List<ConnectionDto> connectionDtoList = new ArrayList<>();
        connections.forEach(connection -> connectionDtoList.add(ConnectionDto.builder()
                .key(connection.getKey())
                .value(connection.getValue())
                .build()));

        return ConnectionOverviewDto.builder()
                .editable(true)
                .connections(connectionDtoList)
                .build();
    }

    public void delete(String projectId, String connectionId) {
        String folderKey = PROJECT_CONNECTION_PREFIX + projectId;
        redisTemplate.opsForHash().delete(folderKey, connectionId);
    }

    public void deleteAll(String projectId) {
        String folderKey = PROJECT_CONNECTION_PREFIX + projectId;
        redisTemplate.opsForHash().entries(folderKey).keySet()
                .forEach(key -> redisTemplate.opsForHash().delete(folderKey, key));
    }

    private Connection jsonToConnection(String jobJson) throws JsonProcessingException {
        return objectMapper.readValue(jobJson, Connection.class);
    }

}
