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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientDefTest {

    @Test
    void noApis() {
        var def = ClientDef.builder()
                .type(ClientDefTest.class)
                .build();

        assertEquals(ClientDefTest.class, def.type());
        assertTrue(def.apis().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> def.apis().add(null));
    }

    @Test
    void immutableApis() {
        var apis = new ArrayList<ApiDef>();
        apis.add(api("hello"));

        var def = ClientDef.builder()
                .type(ClientDefTest.class)
                .apis(apis)
                .build();

        apis.add(api("page"));

        assertEquals(1, def.apis().size());
        assertEquals("hello", def.apis().getFirst().name());
        assertThrows(UnsupportedOperationException.class, () -> def.apis().add(api("other")));
    }

    @Test
    void singularApi() {
        var def = ClientDef.builder()
                .type(ClientDefTest.class)
                .api(api("hello"))
                .api(api("page"))
                .build();

        assertEquals(List.of("hello", "page"),
                def.apis().stream().map(ApiDef::name).toList());
    }

    private static ApiDef api(String name) {
        return ApiDef.builder()
                .name(name)
                .build();
    }
}
