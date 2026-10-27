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
import org.springframework.core.ResolvableType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiDefTest {

    @Test
    void noParameters() {
        var def = ApiDef.builder()
                .name("hello")
                .returnType(ResolvableType.forClass(String.class))
                .build();

        assertEquals("hello", def.name());
        assertEquals(String.class, def.returnType().resolve());
        assertTrue(def.parameters().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> def.parameters().add(null));
    }

    @Test
    void immutableParameters() {
        var parameters = new ArrayList<ApiParameterDef>();
        parameters.add(param("name"));

        var def = ApiDef.builder()
                .name("hello")
                .parameters(parameters)
                .build();

        parameters.add(param("keyword"));

        assertEquals(1, def.parameters().size());
        assertEquals("name", def.parameters().getFirst().name());
        assertThrows(UnsupportedOperationException.class, () -> def.parameters().add(param("page")));
    }

    @Test
    void singularParameter() {
        var def = ApiDef.builder()
                .name("hello")
                .parameter(param("name"))
                .parameter(param("keyword"))
                .build();

        assertEquals(List.of("name", "keyword"),
                def.parameters().stream().map(ApiParameterDef::name).toList());
    }

    private static ApiParameterDef param(String name) {
        return ApiParameterDef.builder()
                .name(name)
                .in(ApiParameterDef.In.QUERY)
                .build();
    }
}
