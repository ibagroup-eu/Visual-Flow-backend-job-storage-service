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

import eu.ibagroup.vfjobstorageservice.dto.exporting.ExportRequestDto;
import eu.ibagroup.vfjobstorageservice.dto.exporting.ExportResponseDto;
import eu.ibagroup.vfjobstorageservice.dto.importing.ImportResponseDto;
import eu.ibagroup.vfjobstorageservice.dto.jobs.JobDto;
import eu.ibagroup.vfjobstorageservice.dto.pipelines.PipelineDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferService {

    private static final String JOB_ID_PROPERTY = "jobId";
    private static final String PIPELINE_ID_PROPERTY = "pipelineId";
    private final JobStorageService jobService;
    private final PipelineService pipelineService;

    /**
     * Export jobs and pipelines.
     *
     * @param projectId        project id
     * @param jobIds           job ids for import
     * @param pipelineRequests pipelines ids and flag with jobs
     * @return pipelines and jobs in json format
     */
    public ExportResponseDto exporting(String projectId, Set<String> jobIds,
                                       Collection<ExportRequestDto.PipelineRequest> pipelineRequests) {
        ExportResponseDto result = new ExportResponseDto();
        insertNestedEntities(projectId, pipelineService.getByIds(projectId, pipelineRequests.stream()
                .filter(ExportRequestDto.PipelineRequest::isWithRelatedEntities)
                .map(ExportRequestDto.PipelineRequest::getPipelineId)
                .collect(Collectors.toSet())), result);
        result.getPipelines().addAll(pipelineService.export(projectId, pipelineRequests.stream()
                .map(ExportRequestDto.PipelineRequest::getPipelineId)
                .collect(Collectors.toSet())));
        result.getJobs().addAll(jobService.export(projectId, jobIds));
        return result;
    }

    /**
     * Secondary method for inserting all nested entities from the required pipelines.
     *
     * @param projectId          is a project ID.
     * @param pipelinesToProcess pipelines with nested entities.
     * @param exportResult       is a result of exporting.
     */
    private void insertNestedEntities(String projectId, List<PipelineDto> pipelinesToProcess,
                                      ExportResponseDto exportResult) {
        pipelinesToProcess.forEach((PipelineDto pipeline) -> {
            exportResult.getJobs().addAll(jobService.getByIds(projectId,
                    getNestedEntityIds(pipeline, JOB_ID_PROPERTY)));
            List<PipelineDto> nestedPipelines = pipelineService.getByIds(projectId,
                    getNestedEntityIds(pipeline, PIPELINE_ID_PROPERTY));
            exportResult.getPipelines().addAll(nestedPipelines);
            insertNestedEntities(projectId, nestedPipelines, exportResult);
        });
    }

    /**
     * Secondary method for finding all nested entities' IDs.
     *
     * @param pipeline  pipeline with nested entities.
     * @param fieldName job or pipeline nested entity type.
     * @return all nested entities' IDs.
     */
    private static Set<String> getNestedEntityIds(PipelineDto pipeline, String fieldName) {
        Set<String> entityIds = new HashSet<>();
        String rawData = pipeline.getDefinition().toPrettyString();
        String rawDataSearch = String.format(".*\"%s\" : \"(.*)\".*", fieldName);
        Pattern rawDataPattern = Pattern.compile(rawDataSearch, Pattern.CASE_INSENSITIVE);
        Matcher rawDataMatcher = rawDataPattern.matcher(rawData);
        while (rawDataMatcher.find()) {
            entityIds.add(rawDataMatcher.group(1));
        }
        return entityIds;
    }

    /**
     * Method for importing jobs and pipelines.
     *
     * @param projectId is project ID.
     * @param jobs      jobs DTOs.
     * @param pipelines pipelines DTOs.
     * @return importing result.
     */
    public ImportResponseDto importing(String projectId, List<JobDto> jobs, List<PipelineDto> pipelines) {
        ImportResponseDto result = new ImportResponseDto();
        if (!CollectionUtils.isEmpty(jobs)) {
            jobService.importAll(projectId, jobs, result);
        }
        if (!CollectionUtils.isEmpty(pipelines)) {
            pipelineService.importAll(projectId, pipelines, result);
        }
        return result;
    }
}
