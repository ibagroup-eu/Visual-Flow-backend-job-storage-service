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
package eu.ibagroup.vfjobstorageservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.ibagroup.vfjobstorageservice.dto.jobs.JobDto;
import eu.ibagroup.vfjobstorageservice.dto.jobs.JobOverviewListDto;
import eu.ibagroup.vfjobstorageservice.services.JobStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/project")
public class JobStorageController {

    private final JobStorageService jobStorageService;

    /**
     * Get all jobs in project.
     *
     * @param projectId project id
     * @return ResponseEntity with jobs graphs
     */
    @Operation(summary = "Get all jobs in a project", description = "Get information about all jobs in a project")
    @GetMapping("{projectId}/job")
    public JobOverviewListDto getAll(@PathVariable String projectId) {
        LOGGER.info(
                "Receiving all jobs in project '{}'",
                projectId
        );
        return jobStorageService.getAll(projectId);
    }

    /**
     * Getting job in project by id.
     *
     * @param projectId project id
     * @param id        job id
     * @return ResponseEntity with job graph
     */
    @Operation(summary = "Get information about the job", description = "Fetch job's structure by id")
    @GetMapping("{projectId}/job/{id}")
    public JobDto get(@PathVariable String projectId, @PathVariable String id) throws JsonProcessingException {
        LOGGER.info(
                "Receiving job '{}' in project '{}'",
                id,
                projectId
        );
        return jobStorageService.get(projectId, id);
    }

    /**
     * Creating new job in project.
     *
     * @param projectId project id
     * @param jobDto    object with name and graph
     * @return ResponseEntity with id of new job
     */
    @Operation(summary = "Create a new job", description = "Create a new job in the project", responses = {
            @ApiResponse(responseCode = "200", description = "Id of a new job")})
    @PostMapping("{projectId}/job")
    public ResponseEntity<String> create(
            @PathVariable String projectId, @Valid @RequestBody JobDto jobDto) throws JsonProcessingException {
        LOGGER.info(
                "Creating new job in project '{}'",
                projectId
        );
        String id = jobStorageService.create(projectId, jobDto);
        LOGGER.info(
                "Job '{}' in project '{}' successfully created",
                id,
                projectId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    /**
     * Updating job in project by id.
     *
     * @param projectId project id
     * @param id        job id
     * @param jobDto    object with name and graph
     */
    @Operation(summary = "Update existing job", description = "Update existing job with a new structure")
    @PostMapping("{projectId}/job/{id}")
    public ResponseEntity<Void> update(
            @PathVariable String projectId,
            @PathVariable String id,
            @Valid @RequestBody JobDto jobDto) {
        LOGGER.info(
                "Updating job '{}' in project '{}'",
                id,
                projectId
        );
        jobStorageService.update(projectId, id, jobDto);
        LOGGER.info(
                "Job '{}' in project '{}' successfully updated",
                id,
                projectId
        );
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update the status of an existing job", description = "Update the status of an existing job")
    @PostMapping("{projectId}/job/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable String projectId,
            @PathVariable String id,
            @RequestParam String status, @RequestBody JobDto jobDto) throws JsonProcessingException {
        LOGGER.info(
                "Updating job '{}' in project '{}'",
                id,
                projectId
        );
        jobStorageService.updateStatus(projectId, id, status, jobDto);
        LOGGER.info(
                "Job '{}' in project '{}' successfully updated",
                id,
                projectId
        );
        return ResponseEntity.noContent().build();
    }

    /**
     * Deleting job in project by id.
     *
     * @param projectId project id
     * @param id        job id
     */
    @Operation(summary = "Delete the job", description = "Delete existing job with all it's instances", responses = {
            @ApiResponse(responseCode = "204", description = "Indicates successful job deletion")})
    @DeleteMapping("{projectId}/job/{id}")
    public ResponseEntity<Void> delete(@PathVariable String projectId, @PathVariable String id) {
        LOGGER.info(
                "Deleting '{}' job in project '{}'",
                id,
                projectId
        );
        jobStorageService.delete(projectId, id);
        LOGGER.info(
                "Job '{}' in project '{}' successfully deleted",
                id,
                projectId
        );
        return ResponseEntity.noContent().build();
    }

    /**
     * Copies job.
     *
     * @param projectId project id
     * @param jobId     job id
     */
    @Operation(summary = "Copy the job", description = "Make a job copy within the same project")
    @PostMapping("{projectId}/job/{jobId}/copy")
    public void copy(@PathVariable String projectId, @PathVariable String jobId) throws JsonProcessingException {
        LOGGER.info("Copying job '{}' in project '{}'", jobId, projectId);
        jobStorageService.copy(projectId, jobId);
    }
}
