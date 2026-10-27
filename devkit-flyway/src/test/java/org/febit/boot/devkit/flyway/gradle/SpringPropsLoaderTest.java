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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringPropsLoaderTest {

    @Test
    void resolveListSingleValue() {
        var props = new Properties();
        props.setProperty("flyway.locations", "filesystem:db/migration");

        assertEquals(List.of("filesystem:db/migration"),
                SpringPropsLoader.resolveList(props, "flyway.locations", 3));
    }

    @Test
    void resolveListIndexed() {
        var props = new Properties();
        props.setProperty("flyway.locations[0]", "a");
        props.setProperty("flyway.locations[1]", "b");
        // The index must be continuous
        props.setProperty("flyway.locations[3]", "d");

        assertEquals(List.of("a", "b"),
                SpringPropsLoader.resolveList(props, "flyway.locations", 5));
    }

    @Test
    void resolveListLimitedByMax() {
        var props = new Properties();
        props.setProperty("flyway.locations[0]", "a");
        props.setProperty("flyway.locations[1]", "b");
        props.setProperty("flyway.locations[2]", "c");

        assertEquals(List.of("a", "b"),
                SpringPropsLoader.resolveList(props, "flyway.locations", 2));
    }

    @Test
    void resolveListAbsent() {
        assertTrue(SpringPropsLoader.resolveList(new Properties(), "flyway.locations", 3).isEmpty());
    }

    @Test
    void yaml(@TempDir Path dir) throws IOException {
        var file = dir.resolve("application.yaml");
        Files.writeString(file, """
                demo:
                  name: yaml-value
                  locations:
                    - a
                    - b
                """);

        var props = SpringPropsLoader.yaml(file.toString());

        assertEquals("yaml-value", props.getProperty("demo.name"));
        assertEquals("a", props.getProperty("demo.locations[0]"));
        assertEquals("b", props.getProperty("demo.locations[1]"));
    }

    @Test
    void properties(@TempDir Path dir) throws IOException {
        var file = dir.resolve("application.properties");
        Files.writeString(file, "demo.name=props-value\n");

        var props = SpringPropsLoader.properties(file.toString());

        assertEquals("props-value", props.getProperty("demo.name"));
    }
}
