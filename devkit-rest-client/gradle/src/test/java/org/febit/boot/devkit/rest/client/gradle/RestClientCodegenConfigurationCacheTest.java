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

import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.febit.boot.devkit.rest.client.gradle.RestClientCodegenScene.applyChange;
import static org.febit.boot.devkit.rest.client.gradle.RestClientCodegenScene.generatedFile;
import static org.febit.boot.devkit.rest.client.gradle.RestClientCodegenScene.outcome;
import static org.febit.boot.devkit.rest.client.gradle.RestClientCodegenScene.run;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestClientCodegenConfigurationCacheTest {

    @BeforeEach
    void setUp() throws IOException {
        RestClientCodegenScene.prepare();
    }

    @Test
    void noDeprecationsAndReusesCache() throws IOException {
        // '--warning-mode=fail' turns every deprecated API usage into a failure,
        // e.g. an access of 'Task.project' from the task action.
        // '--configuration-cache-problems=fail' fails when the entry can not be stored.
        var result = run(
                "--configuration-cache",
                "--configuration-cache-problems=fail",
                "--warning-mode=fail"
        );
        assertEquals(TaskOutcome.SUCCESS, outcome(result));
        assertFalse(result.getOutput().contains("Task.project"));
        assertTrue(result.getOutput().contains("Configuration cache entry stored"),
                result.getOutput());
        assertTrue(generatedFile("DemoController").isFile());

        // Reuse the stored configuration
        result = run(
                "--configuration-cache",
                "--configuration-cache-problems=fail",
                "--warning-mode=fail"
        );
        assertEquals(TaskOutcome.UP_TO_DATE, outcome(result));
        assertFalse(result.getOutput().contains("Task.project"));
        assertTrue(result.getOutput().contains("Reusing configuration cache"),
                result.getOutput());

        // Still works after the inputs changed
        applyChange("changes/build.with-suffix.gradle", "client/build.gradle");
        result = run(
                "--configuration-cache",
                "--configuration-cache-problems=fail",
                "--warning-mode=fail"
        );
        assertEquals(TaskOutcome.SUCCESS, outcome(result));
        assertTrue(generatedFile("DemoControllerApi").isFile());
        assertFalse(result.getOutput().contains("Task.project"));
    }
}
