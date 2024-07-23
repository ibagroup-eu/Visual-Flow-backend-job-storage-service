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
import eu.ibagroup.vfjobstorageservice.dto.exporting.Exportable;
import eu.ibagroup.vfjobstorageservice.dto.importing.ImportResponseDto;
import eu.ibagroup.vfjobstorageservice.dto.importing.Importable;
import eu.ibagroup.vfjobstorageservice.dto.pipelines.PipelineDto;
import eu.ibagroup.vfjobstorageservice.dto.pipelines.PipelineOverviewDto;
import eu.ibagroup.vfjobstorageservice.dto.pipelines.PipelineOverviewListDto;
import eu.ibagroup.vfjobstorageservice.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.beans.FeatureDescriptor;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.ibagroup.vfjobstorageservice.dto.Constants.DRAFT_STATUS;
import static eu.ibagroup.vfjobstorageservice.dto.Constants.PIPELINE_KEY_PREFIX;
import static eu.ibagroup.vfjobstorageservice.dto.Constants.PROJECT_KEY_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class PipelineService implements Exportable<PipelineDto>, Importable<PipelineDto> {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public static String[] findFieldsWithNullValues(Object source) {
        final BeanWrapper wrappedSource = PropertyAccessorFactory.forBeanPropertyAccess(source);
        return Stream.of(wrappedSource.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
                .toArray(String[]::new);
    }

    private static String getFolderKey(String projectId) {
        return PROJECT_KEY_PREFIX + projectId + PIPELINE_KEY_PREFIX;
    }

    public String create(String projectId, PipelineDto pipelineRequestDto) throws JsonProcessingException {
        if (getNamesInProject(projectId).contains(pipelineRequestDto.getName())) {
            throw new BadRequestException(String.format("Pipeline with name '%s' already exist in project '%s'",
                    pipelineRequestDto.getName(), projectId));
        }

        pipelineRequestDto.setId(UUID.randomUUID().toString());
        pipelineRequestDto.setLastModified(Instant.now().toString());
        if (pipelineRequestDto.getStatus() == null) {
            pipelineRequestDto.setStatus("Draft");
        }
        String folderKey = getFolderKey(projectId);
        String pipelineKey = folderKey + pipelineRequestDto.getId();
        String json = objectMapper.writeValueAsString(pipelineRequestDto);
        Boolean created = redisTemplate.opsForHash().putIfAbsent(folderKey, pipelineKey, json);
        if (Boolean.FALSE.equals(created)) {
            throw new DuplicateKeyException("Pipeline with id " + pipelineKey + " already exists");
        }
        return pipelineRequestDto.getId();
    }

    private Set<String> getNamesInProject(String projectId) {
        return getAll(projectId).getPipelines().stream()
                .map(PipelineOverviewDto::getName)
                .collect(Collectors.toSet());
    }

    private String getIdByName(String projectId, String name) {
        return getAll(projectId).getPipelines().stream()
                .filter(pip -> pip.getName().equals(name))
                .findFirst()
                .map(PipelineOverviewDto::getId)
                .orElseThrow(() -> new BadRequestException(
                        "Pipeline with such a name '%s' doesn't exists in project '%s'",
                        name, projectId
                ));
    }

    public PipelineDto getById(String projectId, String id) {
        String folderKey = getFolderKey(projectId);
        String pipelineKey = folderKey + id;
        String value = (String) redisTemplate.opsForHash().get(folderKey, pipelineKey);
        return readJson(value, PipelineDto.class);
    }

    @SneakyThrows
    private <T> T readJson(String value, Class<T> valueType) {
        return objectMapper.readValue(value, valueType);
    }

    @SneakyThrows
    public void update(String projectId, String id, PipelineDto pipelineRequestDto) {
        String folderKey = getFolderKey(projectId);

        redisTemplate.opsForHash().entries(folderKey).forEach((Object key, Object value) -> {
            PipelineDto pipelineDto = readJson((String) value, PipelineDto.class);
            if (Objects.equals(pipelineDto.getName(), pipelineRequestDto.getName())
                    && !Objects.equals(pipelineDto.getId(), id)) {
                throw new BadRequestException(String.format("Pipeline with name '%s' already exist in project '%s'",
                        pipelineDto.getName(), projectId));
            }
        });

        String jobKey = folderKey + id;
        pipelineRequestDto.setId(id);
        pipelineRequestDto.setLastModified(Instant.now().toString());
        if (pipelineRequestDto.getStatus() == null) {
            pipelineRequestDto.setStatus("Draft");
        }
        String json = objectMapper.writeValueAsString(pipelineRequestDto);
        redisTemplate.opsForHash().put(folderKey, jobKey, json);
    }

    public void patch(String projectId, String id, PipelineDto request) {
        PipelineDto response = getById(projectId, id);
        BeanUtils.copyProperties(request, response, findFieldsWithNullValues(request));
        update(projectId, id, response);
    }

    public void delete(String projectId, String id) {
        String folderKey = getFolderKey(projectId);
        String pipelineKey = folderKey + id;
        redisTemplate.opsForHash().delete(folderKey, pipelineKey);
    }

    public PipelineOverviewListDto getAll(String projectId) {
        String folderKey = getFolderKey(projectId);
        List<PipelineOverviewDto> pipelines = redisTemplate.opsForHash().values(folderKey)
                .stream()
                .map(json -> readJson((String) json, PipelineOverviewDto.class))
                .toList();
        return PipelineOverviewListDto.builder()
                .pipelines(pipelines)
                .editable(true)
                .build();
    }

    @Override
    public List<PipelineDto> getByIds(String projectId, Set<String> pipelineIds) {
        if (CollectionUtils.isEmpty(pipelineIds)) {
            return List.of();
        }
        String folderKey = getFolderKey(projectId);
        List<Object> pipKeys = pipelineIds.stream().map(pipId -> folderKey + pipId)
                .collect(Collectors.toList());
        return redisTemplate.opsForHash().multiGet(folderKey, pipKeys)
                .stream()
                .filter(Objects::nonNull)
                .map(json -> readJson(json.toString(), PipelineDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public void importAll(String projectId, List<PipelineDto> pipelines, ImportResponseDto importData) {
        pipelines.forEach((PipelineDto pipeline) -> {
            try {
                pipeline.setJobsStatuses(new HashMap<>());
                pipeline.setStatus(DRAFT_STATUS);
                create(projectId, pipeline);
            } catch (DuplicateKeyException | JsonProcessingException e) {
                LOGGER.error("Error occurred during importing pipelines: {}", e.getMessage());
                importData.addToNotImportedPipelines(pipeline.getName());
                importData.addToErrorsInPipelines(pipeline.getName(), e.getLocalizedMessage());
            } catch (BadRequestException e) {
                LOGGER.info("Pipeline '{}' exists. Updating it: {}", pipeline.getName(), e.getLocalizedMessage());
                update(projectId, getIdByName(projectId, pipeline.getName()), pipeline);
            }
        });
    }

    /**
     * Method for copying a job.
     *
     * @param projectId  is a project ID.
     * @param pipelineId is a pipeline ID.
     */
    public void copy(String projectId, String pipelineId) throws JsonProcessingException {
        PipelineDto pipeline = getById(projectId, pipelineId);
        String currentName = pipeline.getName();
        Set<String> availableNames = getAll(projectId).getPipelines().stream()
                .map(PipelineOverviewDto::getName)
                .filter(name -> name.startsWith(currentName))
                .collect(Collectors.toSet());
        String resultName = CommonUtils.generateNameForCopy(availableNames, currentName);
        pipeline.setId(null);
        pipeline.setName(resultName);
        create(projectId, pipeline);
    }
}
