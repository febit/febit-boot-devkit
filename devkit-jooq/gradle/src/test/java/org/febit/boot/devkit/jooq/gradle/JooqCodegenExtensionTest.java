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
package org.febit.boot.devkit.jooq.gradle;

import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.febit.boot.devkit.jooq.meta.MetaUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JooqCodegenExtensionTest {

    @TempDir
    Path tempDir;

    ProjectInternal project;

    JooqCodegenExtension extension;

    @BeforeEach
    void setUp() {
        JooqCodegenPlugin.INTERNAL_TESTING_MODE.set(true);

        project = (ProjectInternal) ProjectBuilder.builder()
                .withProjectDir(tempDir.toFile())
                .withName("jooq-codegen-extension-test")
                .build();
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(JooqCodegenPlugin.class);

        extension = project.getExtensions()
                .getByType(JooqCodegenExtension.class);
    }

    @Test
    void defaults() {
        assertEquals(List.of("db/migration"), extension.getMigrationsDirs().get());

        assertEquals(new File(tempDir.toFile(), "build/" + JooqCodegenExtension.DIR_GENERATED_SRC),
                extension.getGeneratedSourceDir().get().getAsFile());
        assertEquals(JooqCodegenExtension.DIR_GENERATED_SRC,
                extension.getTargetDir().get());

        var generator = extension.getGenerator().get();
        assertEquals(MetaUtils.CLASS_GENERATOR, generator.getName());
        assertEquals(MetaUtils.CLASS_STRATEGY, generator.getStrategy().getName());
        assertTrue(generator.getDatabase().getForcedTypes().isEmpty());

        assertInstanceOf(PresetJdbcProvider.class, extension.getJdbcProvider().get());
        assertNotNull(extension.getContainer().get());
        assertNotNull(extension.getJdbc().get());
    }

    @Test
    void migrationsDir() {
        extension.migrationsDir("db/extra");

        assertEquals(List.of("db/migration", "db/extra"), extension.getMigrationsDirs().get());
    }

    @Test
    void schema() {
        extension.setSchema("demo");
        extension.setIncludeSchema("other");
        extension.setExcludes("flyway_.*");

        var database = extension.getGenerator().get().getDatabase();
        assertEquals("demo", database.getInputSchema());
        assertEquals(1, database.getSchemata().size());
        assertEquals("other", database.getSchemata().getFirst().getInputSchema());
        assertEquals("flyway_.*", database.getExcludes());
    }

    @Test
    void targetPackage() {
        extension.setTargetPackage("org.demo.jooq");

        assertEquals("org.demo.jooq",
                extension.getGenerator().get().getTarget().getPackageName());
        // The target directory is not affected by the package
        assertEquals(JooqCodegenExtension.DIR_GENERATED_SRC,
                extension.getTargetDir().get());
    }

    @Test
    void jdbcIsSharedWithConfig() {
        var jdbc = extension.getJdbc().get();

        jdbc.setUrl("jdbc:h2:mem:demo");
        jdbc.setUser("demo");

        assertEquals("jdbc:h2:mem:demo",
                extension.getJooqConfig().get().getJdbc().getUrl());
        assertEquals("demo",
                extension.getJooqConfig().get().getJdbc().getUser());
    }

    @Test
    void registeredTasks() {
        assertTrue(project.getTasks().getNames()
                .contains(JooqCodegenPlugin.TASK_GENERATE_JOOQ));
        assertTrue(project.getConfigurations().getNames()
                .contains(JooqCodegenPlugin.RUNTIME));
        assertFalse(project.getTasks().getNames().isEmpty());
    }
}
