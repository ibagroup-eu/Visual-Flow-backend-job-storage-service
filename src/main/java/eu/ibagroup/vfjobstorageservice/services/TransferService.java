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
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferService {

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
        Set<String> pipelineIds = pipelineRequests.stream()
                .map(ExportRequestDto.PipelineRequest::getPipelineId)
                .collect(Collectors.toSet());
        Set<PipelineDto> pipelines = pipelineService.export(projectId, pipelineIds);
        Set<String> pipelineIdsForJobsExport = pipelineRequests.stream()
                .filter(ExportRequestDto.PipelineRequest::isWithRelatedJobs)
                .map(ExportRequestDto.PipelineRequest::getPipelineId)
                .collect(Collectors.toSet());
        pipelines.stream()
                .filter(pipDto -> pipelineIdsForJobsExport.contains(pipDto.getId()))
                .forEach((PipelineDto pipelineDto) -> {
                    String rawData = pipelineDto.getDefinition().toPrettyString();
                    String rawDataSearch = ".*\"jobId\" : \"(.*)\".*";
                    Pattern rawDataPattern = Pattern.compile(rawDataSearch, Pattern.CASE_INSENSITIVE);
                    Matcher rawDataMatcher = rawDataPattern.matcher(rawData);
                    while (rawDataMatcher.find()) {
                        jobIds.add(rawDataMatcher.group(1));
                    }
                });
        return ExportResponseDto
                .builder()
                .jobs(jobService.export(projectId, jobIds))
                .pipelines(pipelines)
                .build();
    }

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
