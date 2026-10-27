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
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

final class RestClientCodegenScene {

    static final String TASK = ":client:generateRestClient";

    static final String GENERATED_PATH =
            "build/generated/sources/codegen-rest-client/org/fixture/client";

    private static final File SCENE_DIR = new File("src/test/scene-rest-client").getAbsoluteFile();
    private static final File WORK_DIR = new File("build/test-plugin-scenes/rest-client").getAbsoluteFile();
    private static final File TEST_KIT_DIR = new File("build/test-plugin-scenes/test-kit").getAbsoluteFile();

    private static final String SPRING_WEB_JAR = jarOf(RestController.class);

    private RestClientCodegenScene() {
    }

    static void prepare() throws IOException {
        FileUtils.deleteDirectory(WORK_DIR);
        FileUtils.copyDirectory(SCENE_DIR, WORK_DIR);
    }

    static File file(String path) {
        return new File(WORK_DIR, path);
    }

    static File generatedFile(String simpleName) {
        return file("client/" + GENERATED_PATH + "/" + simpleName + ".java");
    }

    static String generatedContent(String simpleName) throws IOException {
        return Files.readString(generatedFile(simpleName).toPath());
    }

    static void applyChange(String from, String to) throws IOException {
        Files.copy(file(from).toPath(), file(to).toPath(),
                StandardCopyOption.REPLACE_EXISTING);
    }

    static BuildResult run(String... extraArgs) {
        return runner(extraArgs).build();
    }

    static BuildResult runAndFail(String... extraArgs) {
        return runner(extraArgs).buildAndFail();
    }

    static TaskOutcome outcome(BuildResult result) {
        var task = result.task(TASK);
        assertNotNull(task, "Task " + TASK + " was not executed");
        return task.getOutcome();
    }

    private static GradleRunner runner(String... extraArgs) {
        var args = new ArrayList<String>();
        args.add(TASK);
        args.add("-Pfixture.springWebJar=" + SPRING_WEB_JAR);
        args.addAll(List.of(extraArgs));
        args.add("--stacktrace");

        return GradleRunner.create()
                .withProjectDir(WORK_DIR)
                .withTestKitDir(TEST_KIT_DIR)
                .withPluginClasspath()
                .withArguments(args);
    }

    private static String jarOf(Class<?> cls) {
        try {
            return new File(cls.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
            ).getAbsolutePath().replace('\\', '/');
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
