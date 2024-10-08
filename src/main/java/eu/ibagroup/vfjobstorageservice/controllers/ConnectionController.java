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
import eu.ibagroup.vfjobstorageservice.dto.connections.ConnectionDto;
import eu.ibagroup.vfjobstorageservice.dto.connections.ConnectionOverviewDto;
import eu.ibagroup.vfjobstorageservice.services.ConnectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/project")
public class ConnectionController {

    private final ConnectionService connectionService;

    /**
     * Creating new connection in project.
     *
     * @param projectId
     * @param connectionDto
     * @return
     * @throws JsonProcessingException
     */
    @Operation(summary = "Create connection", description = "Create new connection in existing project")
    @PostMapping("{projectId}/connection")
    public ResponseEntity<String> create(@PathVariable String projectId,
                                         @RequestBody final ConnectionDto connectionDto)
            throws JsonProcessingException {
        LOGGER.info(
                "Creating connection");
        String connectionKey = connectionService.create(projectId, connectionDto);
        LOGGER.info(
                "Connection successfully created");
        return ResponseEntity.status(HttpStatus.CREATED).body(connectionKey);
    }

    /**
     * Getting all connections in a project by project id
     *
     * @param projectId
     * @return
     */
    @Operation(summary = "Get all connections in a project",
            description = "Get information about all connections in a project")
    @GetMapping("{projectId}/connections")
    public ConnectionOverviewDto getAll(@PathVariable String projectId) {
        LOGGER.info(
                "Receiving all connections in project '{}'",
                projectId
        );
        return connectionService.getAll(projectId);
    }

    /**
     * Getting connection in a project by id
     *
     * @param projectId project ID
     * @return connection by its ID.
     */
    @Operation(summary = "Get all connections in a project",
            description = "Get information about all connections in a project")
    @GetMapping("{projectId}/connections/{connectionId}")
    public ConnectionDto get(@PathVariable String projectId, @PathVariable String connectionId) {
        LOGGER.info(
                "Receiving connection '{}' in project '{}'",
                connectionId,
                projectId
        );
        return connectionService.get(projectId, connectionId);
    }

    /**
     * Updating existing connection in a project.
     *
     * @param projectId
     * @param connectionId
     * @param connectionDto
     * @return
     * @throws JsonProcessingException
     */
    @Operation(summary = "Update existing connection", description = "Update existing connection")
    @PutMapping("{projectId}/connections/{connectionId}")
    public ResponseEntity<Void> update(
            @PathVariable String projectId,
            @PathVariable String connectionId,
            @RequestBody ConnectionDto connectionDto) throws JsonProcessingException {
        LOGGER.info(
                "Updating connection '{}' in project '{}'",
                connectionId,
                projectId
        );
        connectionService.update(projectId, connectionDto);
        LOGGER.info(
                "Connection in project '{}' successfully updated by {}",
                connectionId,
                projectId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deleting connection in a project.
     *
     * @param projectId
     * @param connectionId
     * @return
     */
    @Operation(summary = "Delete the connection", description = "Delete existing connection", responses = {
            @ApiResponse(responseCode = "204", description = "Indicates successful connection deletion")})
    @DeleteMapping("{projectId}/connections/{connectionId}")
    public ResponseEntity<Void> delete(@PathVariable String projectId, @PathVariable String connectionId) {
        LOGGER.info(
                "Deleting '{}' connection in project '{}'",
                connectionId,
                projectId
        );
        connectionService.delete(projectId, connectionId);
        LOGGER.info(
                "Connection '{}' in project '{}' successfully deleted",
                connectionId,
                projectId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deleting all connections in a project.
     *
     * @param projectId
     * @return
     */
    @Operation(summary = "Delete all connections", description = "Delete existing connections", responses = {
            @ApiResponse(responseCode = "204", description = "Indicates successful connections deletion")})
    @DeleteMapping("{projectId}/connections")
    public ResponseEntity<Void> deleteAll(@PathVariable String projectId) {
        LOGGER.info(
                "Deleting connections in project '{}'",
                projectId
        );
        connectionService.deleteAll(projectId);
        LOGGER.info(
                "Connections in project '{}' successfully deleted",
                projectId);
        return ResponseEntity.noContent().build();
    }
}
