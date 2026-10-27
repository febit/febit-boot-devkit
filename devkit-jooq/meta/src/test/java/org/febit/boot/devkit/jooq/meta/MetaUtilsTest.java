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
package org.febit.boot.devkit.jooq.meta;

import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Strategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetaUtilsTest {

    @Test
    void createAndInitGenerator() {
        var generator = MetaUtils.createAndInitGenerator();

        assertEquals(MetaUtils.CLASS_GENERATOR, generator.getName());
        assertEquals(MetaUtils.CLASS_STRATEGY, generator.getStrategy().getName());

        assertNotNull(generator.getDatabase());
        assertNotNull(generator.getTarget());

        var generate = generator.getGenerate();
        assertTrue(generate.isPojos());
        assertTrue(generate.isJavaTimeTypes());
        assertTrue(generate.isJpaAnnotations());
        assertTrue(generate.isValidationAnnotations());
        assertEquals(Boolean.FALSE, generate.isFluentSetters());
    }

    @Test
    void initGeneratorKeepsExistingValues() {
        var database = new Database().withInputSchema("demo");
        var strategy = new Strategy().withName("custom.Strategy");
        var generate = new Generate().withPojos(false);

        var generator = new Generator()
                .withDatabase(database)
                .withStrategy(strategy)
                .withGenerate(generate);

        MetaUtils.initGenerator(generator);

        assertSame(database, generator.getDatabase());
        assertSame(generate, generator.getGenerate());
        // the strategy name is owned by the devkit
        assertEquals(MetaUtils.CLASS_STRATEGY, generator.getStrategy().getName());
        assertSame(strategy, generator.getStrategy());
    }

    @Test
    void emitConfig(@TempDir Path dir) throws IOException {
        var generator = MetaUtils.createAndInitGenerator();
        generator.getTarget().withPackageName("org.febit.demo");

        var file = dir.resolve("jooq-codegen.xml").toFile();
        MetaUtils.emitConfig(new Configuration().withGenerator(generator), file);

        assertTrue(file.isFile());
        var content = Files.readString(file.toPath());
        assertTrue(content.contains("<packageName>org.febit.demo</packageName>"));
        assertTrue(content.contains(MetaUtils.CLASS_GENERATOR));
        assertTrue(content.contains(MetaUtils.CLASS_STRATEGY));
    }
}
