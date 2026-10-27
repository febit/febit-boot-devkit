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
package org.febit.boot.devkit.rest.client.meta;

import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestDefTest {

    @Test
    void of() {
        var def = RequestDef.of(mapping(Mapped.class));

        assertEquals(List.of("/demo", "/api/demo"), def.paths());
        assertEquals(List.of(RequestMethod.GET, RequestMethod.POST), def.methods());
        assertEquals(List.of("X-Kind=a"), def.headers());
        assertEquals(List.of("application/json"), def.consumes());
        assertEquals(List.of("application/json"), def.produces());
        assertEquals("/demo", def.getPrimaryPath());
        assertEquals(RequestMethod.GET, def.method());
    }

    @Test
    void ofDefaults() {
        var def = RequestDef.of(mapping(Empty.class));

        assertEquals(List.of(), def.paths());
        assertEquals(List.of(), def.methods());
        assertEquals("", def.getPrimaryPath());
    }

    @Test
    void builderDefaultsToEmptyLists() {
        var def = RequestDef.builder().build();

        assertTrue(def.paths().isEmpty());
        assertTrue(def.methods().isEmpty());
        assertTrue(def.headers().isEmpty());
        assertTrue(def.consumes().isEmpty());
        assertTrue(def.produces().isEmpty());
        assertEquals("", def.getPrimaryPath());
        assertThrows(UnsupportedOperationException.class, () -> def.paths().add("/x"));
    }

    @Test
    void immutableLists() {
        var paths = new ArrayList<>(List.of("/demo"));
        var def = RequestDef.builder()
                .paths(paths)
                .methods(List.of(RequestMethod.GET))
                .build();

        paths.add("/changed");

        assertEquals(List.of("/demo"), def.paths());
        assertThrows(UnsupportedOperationException.class, () -> def.paths().add("/x"));
        assertThrows(UnsupportedOperationException.class, () -> def.methods().add(RequestMethod.POST));
    }

    private static RequestMapping mapping(Class<?> type) {
        return AnnotatedElementUtils.findMergedAnnotation(type, RequestMapping.class);
    }

    @RequestMapping(
            path = {"/demo", "/api/demo"},
            method = {RequestMethod.GET, RequestMethod.POST},
            headers = "X-Kind=a",
            consumes = "application/json",
            produces = "application/json"
    )
    static class Mapped {
    }

    @RequestMapping
    static class Empty {
    }
}
