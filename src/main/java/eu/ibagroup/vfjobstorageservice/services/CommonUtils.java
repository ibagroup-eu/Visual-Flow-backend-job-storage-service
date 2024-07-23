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

import lombok.experimental.UtilityClass;

import java.util.Collection;

/**
 * Common Utility class.
 */
@UtilityClass
public class CommonUtils {

    private static final String RAW_COPY_PATTERN = "%s-Copy";
    private static final String INDEXED_COPY_PATTERN = "%s-Copy%d";

    /**
     * Method for generating a unique name for the new job's copy.
     *
     * @param availableNames all names for this job and copies.
     * @param currentName    copied job's name.
     * @return generated name for a new copy.
     */
    public static String generateNameForCopy(Collection<String> availableNames, String currentName) {
        String resultName;
        if (!availableNames.contains(String.format(RAW_COPY_PATTERN, currentName))) {
            resultName = String.format(RAW_COPY_PATTERN, currentName);
        } else {
            int copyIndex = 1;
            while (availableNames.contains(String.format(INDEXED_COPY_PATTERN, currentName, copyIndex))) {
                copyIndex++;
            }
            resultName = String.format(INDEXED_COPY_PATTERN, currentName, copyIndex);
        }
        return resultName;
    }
}
