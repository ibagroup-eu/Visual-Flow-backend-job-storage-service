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

package eu.ibagroup.vfjobstorageservice.dto.exporting;

import eu.ibagroup.vfjobstorageservice.dto.jobs.JobDto;
import eu.ibagroup.vfjobstorageservice.dto.pipelines.PipelineDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Export response DTO class.
 */
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(description = "DTO with exported jobs/pipelines in serialized JSON format")
public class ExportResponseDto {
    @ArraySchema(arraySchema = @Schema(description = "List of exported jobs' structures"))
    private final Set<JobDto> jobs = new HashSet<>();
    @ArraySchema(arraySchema = @Schema(description = "List of exported pipelines' structures"))
    private final Set<PipelineDto> pipelines = new HashSet<>();
}
