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
import eu.ibagroup.vfjobstorageservice.dto.pipelines.PipelineOverviewListDto;
import eu.ibagroup.vfjobstorageservice.dto.pipelines.PipelineDto;
import eu.ibagroup.vfjobstorageservice.services.PipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Manage requests for pipelines.
 */

@Slf4j
@Tag(name = "Pipeline API", description = "Manage pipelines")
@RequiredArgsConstructor
@RequestMapping("api/project")
@RestController
public class PipelineController {

    private final PipelineService pipelineService;

    /**
     * Create pipeline.
     *
     * @param projectId          project id
     * @param pipelineRequestDto id and graph for pipeline
     * @return ResponseEntity
     */
    @Operation(summary = "Create a new pipeline", description = "Create a new pipeline in the project",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Id of a new pipeline")})
    @PostMapping(value = "{projectId}/pipeline")
    public ResponseEntity<String> create(
            @PathVariable String projectId, @Valid @RequestBody PipelineDto pipelineRequestDto)
            throws JsonProcessingException {
        LOGGER.info(
                "Creating pipeline in project '{}'",
                projectId);
        String id = pipelineService.create(projectId, pipelineRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    /**
     * Get pipeline.
     *
     * @param projectId project id
     * @param id        pipeline id
     * @return pipeline graph
     */
    @Operation(summary = "Get information about the pipeline", description = "Fetch pipeline's structure by id")
    @GetMapping(value = "{projectId}/pipeline/{id}")
    public PipelineDto get(@PathVariable String projectId, @PathVariable String id) {
        LOGGER.info(
                "Receiving pipeline '{}' in project '{}'",
                id,
                projectId);
        return pipelineService.getById(projectId, id);
    }

    /**
     * Update pipeline.
     *
     * @param projectId          project id
     * @param id                 current pipeline id
     * @param pipelineRequestDto new id and graph for pipeline
     */
    @PutMapping(value = "{projectId}/pipeline/{id}")
    @Operation(summary = "Update existing pipeline", description = "Update existing pipeline with a new structure")
    public void update(
            @PathVariable String projectId,
            @PathVariable String id,
            @Valid @RequestBody PipelineDto pipelineRequestDto) {
        LOGGER.info(
                "Updating pipeline '{}' in project '{}'",
                id,
                projectId);
        pipelineService.update(projectId, id, pipelineRequestDto);
    }

    /**
     * Partial update pipeline.
     *
     * @param projectId          project id
     * @param id                 current pipeline id
     * @param pipelineRequestDto new id and graph for pipeline
     */
    @PatchMapping(value = "{projectId}/pipeline/{id}")
    @Operation(summary = "Update existing pipeline", description = "Patch existing pipeline with a new values")
    public void patch(
            @PathVariable String projectId,
            @PathVariable String id,
            @Valid @RequestBody PipelineDto pipelineRequestDto) {
        LOGGER.info(
                "Patching pipeline '{}' in project '{}'",
                id,
                projectId);
        pipelineService.patch(projectId, id, pipelineRequestDto);
    }

    /**
     * Delete pipeline.
     *
     * @param projectId project id
     * @param id        pipeline id
     */
    @Operation(summary = "Delete the pipeline", description = "Delete existing pipeline", responses =
            {@ApiResponse(responseCode = "204", description = "Indicates successful pipeline deletion")})
    @DeleteMapping(value = "{projectId}/pipeline/{id}")
    public ResponseEntity<Void> delete(@PathVariable String projectId, @PathVariable String id) {
        LOGGER.info(
                "Deleting pipeline '{}' in project '{}'",
                id,
                projectId);
        pipelineService.delete(projectId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all pipelines in project.
     *
     * @param projectId project id
     * @return ResponseEntity with jobs graphs
     */
    @Operation(summary = "Get all pipelines in a project", description = "Get information about all pipelines in" +
            " a project")
    @GetMapping("{projectId}/pipeline")
    public PipelineOverviewListDto getAll(@PathVariable String projectId) {
        LOGGER.info(
                "Receiving all pipelines in project '{}'",
                projectId);
        return pipelineService.getAll(projectId);
    }

    /**
     * Copies pipeline.
     *
     * @param projectId  project id
     * @param pipelineId pipelineId id
     */
    @Operation(summary = "Copy the pipeline", description = "Make a pipeline copy within the same project")
    @PostMapping("{projectId}/pipeline/{pipelineId}/copy")
    public void copy(@PathVariable String projectId, @PathVariable String pipelineId) throws JsonProcessingException {
        LOGGER.info("Copying pipeline '{}' in project '{}'", pipelineId, projectId);
        pipelineService.copy(projectId, pipelineId);
    }

}
