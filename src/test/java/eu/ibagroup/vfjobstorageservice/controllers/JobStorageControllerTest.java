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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;

import eu.ibagroup.vfjobstorageservice.dto.jobs.JobOverviewDto;
import eu.ibagroup.vfjobstorageservice.dto.jobs.JobOverviewListDto;
import eu.ibagroup.vfjobstorageservice.dto.jobs.JobDto;
import eu.ibagroup.vfjobstorageservice.dto.jobs.JobParams;
import eu.ibagroup.vfjobstorageservice.services.JobStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobStorageControllerTest {
    @Mock
    private JobStorageService jobService;
    private JobStorageController controller;

    @BeforeEach
    void setUp() {
        controller = new JobStorageController(jobService);
    }

    @Test
    void testGetAll() {
        when(jobService.getAll("project1")).thenReturn(JobOverviewListDto
                .builder()
                .jobs(List.of(JobOverviewDto
                                .builder()
                                .pipelineInstances(List.of())
                                .build(),
                        JobOverviewDto
                                .builder()
                                .pipelineInstances(List.of())
                                .build()))
                .editable(true)
                .build());

        JobOverviewListDto response = controller.getAll("project1");
        assertEquals(2, response.getJobs().size(), "Jobs size must be 2");
        assertTrue(response.isEditable(), "Must be true");

        verify(jobService).getAll(anyString());
    }

    @Test
    void testCreate() throws JsonProcessingException {
        JobDto jobDto = JobDto
                .builder()
                .definition(new ObjectMapper().readTree("{\"graph\":[]}"))
                .name("newName")
                .params(JobParams.builder().driverCores("1").build())
                .build();
        when(jobService.create("projectId", jobDto)).thenReturn("jobId");
        ResponseEntity<String> response = controller.create("projectId", jobDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Status must be OK");
        assertEquals("jobId", response.getBody(), "Body must be equals to jobId");

        verify(jobService).create(anyString(), any());
    }

    @Test
    void testUpdate() throws JsonProcessingException {
        JobDto jobDto = JobDto
                .builder()
                .definition(new ObjectMapper().readTree("{\"graph\":[]}"))
                .name("newName")
                .params(JobParams.builder().driverCores("1").build())
                .build();
        doNothing().when(jobService).update("projectId", "jobId", jobDto);

        controller.update("projectId", "jobId", jobDto);

        verify(jobService).update(anyString(), anyString(), any());
    }

    @Test
    void testUpdateStatus() throws JsonProcessingException {
        doNothing().when(jobService).updateStatus("projectId", "jobId", "newStatus", JobDto.builder().build());

        controller.updateStatus("projectId", "jobId", "newStatus", JobDto.builder().build());

        verify(jobService).updateStatus(anyString(), anyString(), any(), any());

    }

    @Test
    void testGet() throws IOException {
        JobDto dto = JobDto
                .builder()
                .lastModified("lastModified")
                .definition(new ObjectMapper().readTree("{\"graph\":[]}".getBytes()))
                .name("name")
                .build();

        when(jobService.get("project1", "jobId")).thenReturn(dto);

        JobDto response = controller.get("project1", "jobId");

        assertEquals(dto, response, "Response must be equal to dto");

        verify(jobService).get(anyString(), anyString());
    }

    @Test
    void testDelete() {
        doNothing().when(jobService).delete("project1", "jobId");

        ResponseEntity<Void> response = controller.delete("project1", "jobId");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Status must be 204");

        verify(jobService).delete(anyString(), anyString());
    }
}
