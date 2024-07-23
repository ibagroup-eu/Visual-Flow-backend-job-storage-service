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

package eu.ibagroup.vfjobstorageservice.dto.jobs;

import eu.ibagroup.vfjobstorageservice.dto.ResourceUsageDto;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.Set;

/**
 * job overview DTO class.
 */
@Builder
@EqualsAndHashCode
@Getter
@ToString
@Data
public class JobOverviewDto {
    @Id
    private final String id;
    private final String name;
    private long runId;
    private final String startedAt;
    private final String finishedAt;
    private final String status;
    private final String lastModified;
    private final ResourceUsageDto usage;
    private final List<PipelineJobOverviewDto> pipelineInstances;
    private final String pipelineId;
    private final boolean runnable;
    private final List<String> tags;
    private final Set<String> dependentPipelineIds;
}
