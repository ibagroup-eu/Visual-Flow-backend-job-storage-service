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
import eu.ibagroup.vfjobstorageservice.dto.jobs.JobDto;
import eu.ibagroup.vfjobstorageservice.dto.jobs.JobOverviewDto;
import eu.ibagroup.vfjobstorageservice.dto.jobs.JobOverviewListDto;
import eu.ibagroup.vfjobstorageservice.exceptions.BadRequestException;
import eu.ibagroup.vfjobstorageservice.exceptions.JsonParseException;
import eu.ibagroup.vfjobstorageservice.mappers.JobDtoMapper;
import eu.ibagroup.vfjobstorageservice.mappers.JobOverviewMapper;
import eu.ibagroup.vfjobstorageservice.model.Job;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static eu.ibagroup.vfjobstorageservice.dto.Constants.*;


@Slf4j
@Service
public class JobStorageService implements Exportable<JobDto>, Importable<JobDto> {

    private final RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper;

    public JobStorageService(@Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate,
                             ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public String create(String projectId, JobDto jobDto) throws JsonProcessingException {
        String folderKey = PROJECT_KEY_PREFIX + projectId;
        redisTemplate.opsForHash().entries(folderKey).forEach((Object key, Object value) -> {
            try {
                if (jsonToJob((String) value).getName().equals(jobDto.getName())) {
                    throw new BadRequestException(String.format("Job with name '%s' already exist in project '%s'",
                            jobDto.getName(), projectId));
                }
            } catch (JsonProcessingException e) {
                LOGGER.error(GET_ALL_ERROR + e.getMessage());
                throw new JsonParseException(e.getMessage());
            }
        });
        Job job = JobDtoMapper.INSTANCE.dtoToEntity(jobDto);
        job.setRunnable(!job.getDefinition().get("graph").isEmpty());
        String jobId = job.getId();
        String jobKey = folderKey + JOB_KEY_PREFIX + jobId;
        String jobJson = objectMapper.writeValueAsString(job);
        redisTemplate.opsForHash().put(folderKey, jobKey, jobJson);
        return job.getId();

    }

    public JobOverviewListDto getAll(String projectId) {
        List<JobOverviewDto> jobs = new ArrayList<>();
        String folderKey = PROJECT_KEY_PREFIX + projectId;
        redisTemplate.opsForHash().entries(folderKey).forEach((Object key, Object value) -> {
            try {
                jobs.add(JobOverviewMapper.INSTANCE.entityToDto(jsonToJob((String) value)));
            } catch (JsonProcessingException e) {
                LOGGER.error(GET_ALL_ERROR + e.getMessage());
                throw new JsonParseException(e.getMessage());
            }
        });
        List<JobOverviewDto> result = new ArrayList<>();
        jobs.forEach((JobOverviewDto job) -> {
            String status = job.getStatus();
            if (job.getStatus() == null) {
                status = DRAFT_STATUS;
            }
            result.add(JobOverviewDto.builder()
                    .id(job.getId())
                    .name(job.getName())
                    .runId(job.getRunId())
                    .startedAt(job.getStartedAt())
                    .finishedAt(job.getFinishedAt())
                    .status(status)
                    .lastModified(job.getLastModified())
                    .usage(job.getUsage())
                    .pipelineInstances(new ArrayList<>())
                    .pipelineId(null)
                    .runnable(job.isRunnable())
                    .tags(new ArrayList<>())
                    .dependentPipelineIds(new HashSet<>())
                    .build());
        });
        return JobOverviewListDto.builder()
                .jobs(result)
                .editable(true)
                .build();
    }

    public JobDto get(String projectId, String jobId) throws JsonProcessingException {
        String folderKey = PROJECT_KEY_PREFIX + projectId;
        String jobKey = folderKey + JOB_KEY_PREFIX + jobId;
        Job job = jsonToJob((String) redisTemplate.opsForHash().get(folderKey, jobKey));
        if (job.getStatus() == null) {
            job.setStatus(DRAFT_STATUS);
        }
        JobDto jobDto = JobDtoMapper.INSTANCE.entityToDto(job);
        jobDto.setEditable(true);
        return jobDto;
    }

    @Override
    public List<JobDto> getByIds(String projectId, Set<String> jobIds) {
        if (CollectionUtils.isEmpty(jobIds)) {
            return List.of();
        }
        String folderKey = PROJECT_KEY_PREFIX + projectId;
        List<Object> jobKeys = jobIds.stream().map(jobId -> folderKey + JOB_KEY_PREFIX + jobId)
                .collect(Collectors.toList());
        return redisTemplate.opsForHash().multiGet(folderKey, jobKeys)
                .stream()
                .filter(Objects::nonNull)
                .map((Object json) -> {
                    try {
                        Job job = jsonToJob(json.toString());
                        if (job.getStatus() == null) {
                            job.setStatus(DRAFT_STATUS);
                        }
                        JobDto jobDto = JobDtoMapper.INSTANCE.entityToDto(job);
                        jobDto.setEditable(true);
                        return jobDto;
                    } catch (JsonProcessingException e) {
                        LOGGER.error("Error has been occurred during getting all jobs by ID: {}", e.getMessage());
                        return null;
                    }
                }).collect(Collectors.toList());
    }

    public void delete(String projectId, String jobId) {
        String folderKey = PROJECT_KEY_PREFIX + projectId;
        String jobKey = folderKey + JOB_KEY_PREFIX + jobId;
        redisTemplate.opsForHash().delete(folderKey, jobKey);
    }

    @SneakyThrows
    public void update(String projectId, String jobId, JobDto jobDto) {
        String folderKey = PROJECT_KEY_PREFIX + projectId;
        redisTemplate.opsForHash().entries(folderKey).forEach((Object key, Object value) -> {
            try {
                Job job = jsonToJob((String) value);
                if (job.getName().equals(jobDto.getName()) && !Objects.equals(job.getId(), jobId)) {
                    throw new BadRequestException(String.format("Job with name '%s' already exist in project '%s'",
                            jobDto.getName(), projectId));
                }
            } catch (JsonProcessingException e) {
                LOGGER.error(GET_ALL_ERROR + e.getMessage());
                throw new JsonParseException(e.getMessage());
            }
        });
        String jobKey = folderKey + JOB_KEY_PREFIX + jobId;
        Job jobFromDB = jsonToJob((String) redisTemplate.opsForHash().get(folderKey, jobKey));
        jobFromDB.setDefinition(jobDto.getDefinition());
        jobFromDB.setParams(jobDto.getParams());
        jobFromDB.setRunnable(!jobDto.getDefinition().get("graph").isEmpty());
        jobFromDB.setName(jobDto.getName());
        if (jobDto.getStatus() != null) {
            jobFromDB.setStatus(jobDto.getStatus());
        }
        if (jobDto.getRunId() != 0) {
            jobFromDB.setRunId(jobDto.getRunId());
        }
        jobFromDB.setLastModified(jobDto.getLastModified());
        String jobJson = objectMapper.writeValueAsString(jobFromDB);
        redisTemplate.opsForHash().put(folderKey, jobKey, jobJson);
    }

    public void updateStatus(String projectId, String jobId, String status, JobDto jobDto)
            throws JsonProcessingException {
        String folderKey = PROJECT_KEY_PREFIX + projectId;
        String jobKey = folderKey + JOB_KEY_PREFIX + jobId;
        Job job = jsonToJob((String) redisTemplate.opsForHash().get(folderKey, jobKey));
        job.setStatus(status);
        job.setStartedAt(jobDto.getStartedAt());
        job.setFinishedAt(jobDto.getFinishedAt());
        String jobJson = objectMapper.writeValueAsString(job);
        redisTemplate.opsForHash().put(folderKey, jobKey, jobJson);
    }

    private Job jsonToJob(String jobJson) throws JsonProcessingException {
        return objectMapper.readValue(jobJson, Job.class);
    }

    @Override
    public void importAll(String projectId, List<JobDto> jobs, ImportResponseDto importData) {
        jobs.forEach((JobDto job) -> {
            try {
                job.setStatus(DRAFT_STATUS);
                create(projectId, job);
            } catch (JsonProcessingException e) {
                LOGGER.error("Error occurred during importing jobs: {}", e.getMessage());
                importData.addToNotImportedJobs(job.getName());
                importData.addToErrorsInJobs(job.getName(), e.getLocalizedMessage());
            } catch (BadRequestException e) {
                LOGGER.info("Job '{}' exists. Updating it: {}", job.getName(), e.getLocalizedMessage());
                update(projectId, getIdByName(projectId, job.getName()), job);
            }
        });
    }

    private String getIdByName(String projectId, String name) {
        return getAll(projectId).getJobs().stream()
                .filter(pip -> pip.getName().equals(name))
                .findFirst()
                .map(JobOverviewDto::getId)
                .orElseThrow(() -> new BadRequestException(
                        "Job with such a name '%s' doesn't exists in project '%s'",
                        name, projectId
                ));
    }

    /**
     * Method for copying a job.
     *
     * @param projectId is a project ID.
     * @param jobId     is a job ID.
     */
    public void copy(String projectId, String jobId) throws JsonProcessingException {
        JobDto job = get(projectId, jobId);
        String currentName = job.getName();
        Set<String> availableNames = getAll(projectId).getJobs().stream()
                .map(JobOverviewDto::getName)
                .filter(name -> name.startsWith(currentName))
                .collect(Collectors.toSet());
        String resultName = CommonUtils.generateNameForCopy(availableNames, currentName);
        job.setId(null);
        job.setName(resultName);
        create(projectId, job);
    }
}
