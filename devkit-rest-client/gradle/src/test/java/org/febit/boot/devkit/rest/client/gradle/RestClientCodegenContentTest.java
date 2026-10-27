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

import static org.febit.boot.devkit.rest.client.gradle.RestClientCodegenScene.generatedContent;
import static org.febit.boot.devkit.rest.client.gradle.RestClientCodegenScene.outcome;
import static org.febit.boot.devkit.rest.client.gradle.RestClientCodegenScene.run;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestClientCodegenContentTest {

    @BeforeEach
    void setUp() throws IOException {
        RestClientCodegenScene.prepare();
    }

    @Test
    void emitRequiredAndDeprecated() throws IOException {
        assertEquals(TaskOutcome.SUCCESS, outcome(run()));

        var content = generatedContent("DemoController");

        // Client level
        assertTrue(content.contains("@HttpExchange(\n        value = \"/demo\""));

        // Optional query parameter
        assertTrue(content.contains("@RequestParam(value = \"name\", required = false)"));

        // Required query parameter, path variable and optional header
        assertTrue(content.contains("@PathVariable(\"id\")"));
        assertTrue(content.contains("@RequestParam(\"verbose\")"));
        assertTrue(content.contains("@RequestHeader(value = \"X-Token\", required = false)"));

        // Deprecated method and parameter
        assertTrue(content.contains("    @Deprecated\n    @GetExchange("));
        assertTrue(content.contains("@Deprecated @RequestParam(value = \"old\", required = false)"));
    }

    @Test
    void emitDeprecatedClient() throws IOException {
        assertEquals(TaskOutcome.SUCCESS, outcome(run()));

        var content = generatedContent("LegacyController");

        assertTrue(content.contains("@Deprecated\n@HttpExchange("));
        assertTrue(content.contains("public interface LegacyController"));
        assertTrue(content.contains("url = \"/ping\""));
    }
}
