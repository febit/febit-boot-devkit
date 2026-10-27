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

import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.febit.boot.devkit.rest.client.gradle.RestClientCodegenScene.applyChange;
import static org.febit.boot.devkit.rest.client.gradle.RestClientCodegenScene.file;
import static org.febit.boot.devkit.rest.client.gradle.RestClientCodegenScene.generatedContent;
import static org.febit.boot.devkit.rest.client.gradle.RestClientCodegenScene.generatedFile;
import static org.febit.boot.devkit.rest.client.gradle.RestClientCodegenScene.outcome;
import static org.febit.boot.devkit.rest.client.gradle.RestClientCodegenScene.run;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestClientCodegenCachingTest {

    @BeforeEach
    void setUp() throws IOException {
        RestClientCodegenScene.prepare();
    }

    @Test
    void skippedWhenNothingChanged() throws IOException {
        var result = run();
        assertEquals(TaskOutcome.SUCCESS, outcome(result));
        assertTrue(generatedFile("DemoController").isFile());

        result = run();
        assertEquals(TaskOutcome.UP_TO_DATE, outcome(result));
    }

    @Test
    void rerunOnSourceClassesChanged() throws IOException {
        assertEquals(TaskOutcome.SUCCESS, outcome(run()));
        assertTrue(generatedContent("DemoController").contains("\"/hello\""));

        applyChange("changes/DemoController.java",
                "api/src/main/java/org/fixture/api/DemoController.java");

        assertEquals(TaskOutcome.SUCCESS, outcome(run()));
        assertTrue(generatedContent("DemoController").contains("\"/hello2\""));
    }

    @Test
    void rerunOnConfigChanged() throws IOException {
        assertEquals(TaskOutcome.SUCCESS, outcome(run()));
        assertTrue(generatedFile("DemoController").isFile());
        assertFalse(generatedFile("DemoControllerApi").exists());

        applyChange("changes/build.with-suffix.gradle", "client/build.gradle");

        assertEquals(TaskOutcome.SUCCESS, outcome(run()));
        assertTrue(generatedFile("DemoControllerApi").isFile());
    }

    @Test
    void loadedFromBuildCache() throws IOException {
        // The build cache of the test kit may already hold the entry.
        var first = outcome(run("--build-cache"));
        assertTrue(first == TaskOutcome.SUCCESS || first == TaskOutcome.FROM_CACHE);

        FileUtils.deleteDirectory(file("client/" + RestClientCodegenScene.GENERATED_PATH));

        var result = run("--build-cache");
        assertEquals(TaskOutcome.FROM_CACHE, outcome(result));
        assertTrue(generatedFile("DemoController").isFile());
    }
}
