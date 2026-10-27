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
package org.febit.boot.devkit.flyway.gradle;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.febit.boot.devkit.flyway.gradle.FlywayPlugin.RUNTIME;
import static org.febit.boot.devkit.flyway.gradle.FlywayPlugin.TASK_PREFIX;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlywayPluginTest {

    static final String TASK_INFO = TASK_PREFIX + "info-mix";

    final AtomicBoolean evaluated = new AtomicBoolean(false);

    ProjectInternal project;

    @BeforeEach
    void setUp() throws IOException {
        var projectDir = new File("build/test-plugin-scenes/flyway-plugin");
        FileUtils.deleteDirectory(projectDir);
        FileUtils.copyDirectory(new File("src/test/scene-flyway"), projectDir);

        project = (ProjectInternal) ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .withName("flyway-plugin-test")
                .build();

        project.afterEvaluate(p -> evaluated.set(true));
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(FlywayPlugin.class);

        var extension = project.getExtensions().getByType(FlywayExtension.class);
        extension.getApplicationPropsDir().set(project.getLayout().getProjectDirectory());
        extension.excludeTask(".*-excluded");

        initRuntimeDependencies(project);
    }

    @Test
    void registeredTasks() {
        // Tasks are created on evaluate
        assertFalse(evaluated.get());

        project.evaluate();
        assertTrue(evaluated.get());

        var names = project.getTasks().getNames();
        for (var action : FlywayAction.values()) {
            assertTrue(names.contains(TASK_PREFIX + action.title() + "-mix"),
                    "Missing task for action: " + action);
        }

        assertFalse(names.contains(TASK_PREFIX + "info-excluded"));
        assertFalse(names.contains(TASK_PREFIX + "clean-excluded"));
        assertFalse(names.contains(TASK_PREFIX + "migrate-excluded"));
    }

    @Test
    void resolvedOptions() {
        project.evaluate();

        var task = infoTask();
        var jdbc = task.getJdbc().get();
        var option = task.getOption().get();

        assertEquals("jdbc:h2:mem:flyway-test", jdbc.url());
        assertEquals("devkit", jdbc.user());
        assertEquals("boot&devkit", jdbc.password());

        assertEquals("gbk", option.encoding());
        assertEquals("flyway_schema_history_v1", option.table());
        assertEquals("flyway_history_schema", option.tablespace());
        assertEquals(List.of("devkit", "boot"), option.schemas());
    }

    @Test
    void executesTask() {
        project.evaluate();

        var task = infoTask();
        assertDoesNotThrow(() -> executeTask(task));

        assertEquals(List.of(
                "classpath:db/migration",
                "classpath:db/migration2"
        ), task.getOption().get().migrationsDirs());
    }

    private FlywayTask infoTask() {
        return (FlywayTask) project.getTasks().getByName(TASK_INFO);
    }

    private static void executeTask(FlywayTask task) {
        task.getTaskActions()
                .forEach(action -> action.execute(task));
    }

    private static void initRuntimeDependencies(ProjectInternal project) {
        var springBootVersion = System.getProperty("APP_SPRING_BOOT_VERSION");
        if (StringUtils.isEmpty(springBootVersion)) {
            throw new IllegalStateException(
                    "Please set system property: APP_SPRING_BOOT_VERSION for plugin testing");
        }

        // TODO remote repos
        project.getRepositories().mavenCentral();

        var deps = project.getDependencies();
        deps.add(RUNTIME, deps.enforcedPlatform(
                "org.springframework.boot:spring-boot-dependencies:" + springBootVersion
        ));
        deps.add(RUNTIME, "org.slf4j:slf4j-simple");
        deps.add(RUNTIME, "com.h2database:h2");
        deps.add(RUNTIME, "org.postgresql:postgresql");
        deps.add(RUNTIME, "com.mysql:mysql-connector-j");
    }
}
