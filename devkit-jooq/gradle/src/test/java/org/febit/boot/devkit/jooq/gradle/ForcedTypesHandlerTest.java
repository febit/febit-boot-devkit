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

import org.junit.jupiter.api.Test;

import org.febit.boot.devkit.jooq.meta.ForcedTypes;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForcedTypesHandlerTest {

    @Test
    void timeToInstant() {
        var handler = new ForcedTypesHandler();

        handler.timeToInstant();

        assertEquals(2, handler.size());
        assertTrue(handler.stream().anyMatch(type ->
                Instant.class.getName().equals(type.getUserType())
                        && ForcedTypes.Converters.LOCAL_DATE_TIME_INSTANT.equals(type.getConverter())
        ));
        assertTrue(handler.stream().anyMatch(type ->
                ForcedTypes.Converters.OFFSET_DATE_TIME_INSTANT.equals(type.getConverter())
        ));
    }

    @Test
    void mapping() {
        var handler = new ForcedTypesHandler();

        handler.mapping(Map.of(
                "demo",
                Map.of("status", "enum:org.demo.FooStatus")
        ));

        assertEquals(1, handler.size());
        var type = handler.get(0);
        assertEquals(".*\\.demo\\.status", type.getIncludeExpression());
        assertEquals("org.demo.FooStatus", type.getUserType());
        assertEquals(ForcedTypes.Converters.VALUED_ENUM + ".forEnum(org.demo.FooStatus.class)",
                type.getConverter());
    }

    @Test
    void mappingMultipleColumns() {
        var handler = new ForcedTypesHandler();

        handler.mapping(Map.of(
                "foo", Map.of("enabled", "boolean"),
                "bar", Map.of("deleted", "boolean")
        ));

        assertEquals(2, handler.size());
        assertTrue(handler.stream().anyMatch(type ->
                ".*\\.foo\\.enabled".equals(type.getIncludeExpression())
        ));
        assertTrue(handler.stream().anyMatch(type ->
                ".*\\.bar\\.deleted".equals(type.getIncludeExpression())
        ));
    }

    @Test
    void mappingJsonColumn() {
        var handler = new ForcedTypesHandler();

        handler.mapping(Map.of(
                "foo",
                Map.of("payload", "json:org.demo.JsonBean")
        ));

        // jsonb / json / string
        assertEquals(3, handler.size());
        handler.forEach(type ->
                assertEquals(".*\\.foo\\.payload", type.getIncludeExpression())
        );
    }
}
