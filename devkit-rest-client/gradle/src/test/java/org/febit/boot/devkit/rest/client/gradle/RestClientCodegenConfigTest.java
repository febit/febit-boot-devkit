/*
 * Copyright 2022-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.boot.devkit.rest.client.gradle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.febit.boot.devkit.rest.client.gradle.RestClientCodegenScene.applyChange;
import static org.febit.boot.devkit.rest.client.gradle.RestClientCodegenScene.runAndFail;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestClientCodegenConfigTest {

    @BeforeEach
    void setUp() throws IOException {
        RestClientCodegenScene.prepare();
    }

    @Test
    void failsWithoutSourceProject() throws IOException {
        applyChange("changes/build.without-source-project.gradle", "client/build.gradle");

        var result = runAndFail();

        assertTrue(result.getOutput().contains("sourceProject is not set"));
    }
}
