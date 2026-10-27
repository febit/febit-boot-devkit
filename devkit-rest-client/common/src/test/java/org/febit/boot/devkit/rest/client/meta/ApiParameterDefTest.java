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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiParameterDefTest {

    static final Method PARAMS_METHOD = paramsMethod();

    @Test
    void pathVariable() {
        var def = defs().get("id");

        assertEquals(ApiParameterDef.In.PATH_VAR, def.in());
        assertEquals("id", def.bindName());
        assertTrue(def.required());
        assertFalse(def.deprecated());
        assertEquals(String.class, def.type().resolve());
    }

    @Test
    void optionalQueryParameter() {
        var def = defs().get("keyword");

        assertEquals(ApiParameterDef.In.QUERY, def.in());
        assertEquals("keyword", def.bindName());
        assertFalse(def.required());
    }

    @Test
    void requiredQueryParameter() {
        var def = defs().get("page");

        assertEquals(ApiParameterDef.In.QUERY, def.in());
        assertEquals("page", def.bindName());
        assertTrue(def.required());
        assertEquals(int.class, def.type().resolve());
    }

    @Test
    void optionalHeader() {
        var def = defs().get("token");

        assertEquals(ApiParameterDef.In.HEADER, def.in());
        assertEquals("X-Token", def.bindName());
        assertFalse(def.required());
    }

    @Test
    void body() {
        var def = defs().get("body");

        assertEquals(ApiParameterDef.In.BODY, def.in());
        assertTrue(def.required());
        assertNull(def.bindName());
    }

    @Test
    void modelAttribute() {
        var def = defs().get("filter");

        assertEquals(ApiParameterDef.In.MODEL_ATTR, def.in());
        assertTrue(def.required());
        assertNull(def.bindName());
    }

    @Test
    void noAnnotationIsQueryMap() {
        var def = defs().get("plain");

        assertEquals(ApiParameterDef.In.QUERY_MAP, def.in());
        assertTrue(def.required());
    }

    @Test
    void deprecatedParameter() {
        var def = defs().get("legacy");

        assertEquals(ApiParameterDef.In.QUERY, def.in());
        assertTrue(def.deprecated());
    }

    private static Map<String, ApiParameterDef> defs() {
        return Stream.of(PARAMS_METHOD.getParameters())
                .map(ApiParameterDef::of)
                .collect(Collectors.toMap(ApiParameterDef::name, def -> def));
    }

    private static Method paramsMethod() {
        try {
            return Fixture.class.getDeclaredMethod("params", String.class, String.class,
                    int.class, String.class, String.class, String.class, String.class, String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings({"unused", "DeprecatedIsStillUsed"})
    static class Fixture {

        void params(
                @PathVariable("id") String id,
                @RequestParam(value = "keyword", required = false) String keyword,
                @RequestParam("page") int page,
                @RequestHeader(value = "X-Token", required = false) String token,
                @RequestBody String body,
                @ModelAttribute("filter") String filter,
                @Deprecated @RequestParam(value = "legacy", required = false) String legacy,
                String plain
        ) {
        }
    }
}
