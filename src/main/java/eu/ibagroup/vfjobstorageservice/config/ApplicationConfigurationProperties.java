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
package eu.ibagroup.vfjobstorageservice.config;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Application Configuration (from yaml) class.
 * Represents properties from this configuration.
 */
@Data
@Component
@ConfigurationProperties
@Validated
public class ApplicationConfigurationProperties {

    @Valid
    private OauthSettings oauth;
    @Valid
    private ServerSettings server;
    @Valid
    private RedisSettings redis;

    /**
     * Represents oauth and user management settings.
     */
    @Data
    public static class OauthSettings {
        private OauthUrlSettings url;
        private String provider;
    }

    /**
     * Represents settings, connected with oauth URL.
     */
    @Data
    public static class OauthUrlSettings {
        private String userInfo;
    }

    /**
     * Represents service settings.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServerSettings {
        private String host;
    }

    /**
     * Represents Redis settings.
     */
    @Data
    public static class RedisSettings {
        private String host;
        private Integer port;
    }
}
