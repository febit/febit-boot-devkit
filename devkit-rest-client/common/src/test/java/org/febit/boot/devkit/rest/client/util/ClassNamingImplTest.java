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
package org.febit.boot.devkit.rest.client.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ClassNamingImplTest {

    @Test
    void resolve() {
        var naming = new ClassNamingImpl()
                .prefix("X")
                .suffix("Api")
                .replacePackage("org.a", "org.b")
                .replaceSuffix("Controller", "Client");

        assertEquals("org.b.c.XDemoClientApi", naming.resolve("org.a.c.DemoController"));
        assertEquals("XDemoClientApi", naming.resolve("DemoController"));
    }

    @Test
    void resolveDirectMapping() {
        var naming = new ClassNamingImpl()
                .prefix("X")
                .replace("org.a.Foo", "org.b.Bar");

        assertEquals("org.b.Bar", naming.resolve("org.a.Foo"));
    }

    @Test
    void signatureDescribesRules() {
        var naming = new ClassNamingImpl()
                .prefix("X")
                .replacePackage("org.a", "org.b");

        var signature = naming.signature();

        assertEquals("prefix=X,suffix=,mapping={},packages=[org.a->org.b],suffixes=[]", signature);
        assertEquals(signature, naming.signature());
    }

    @Test
    void signatureIgnoresMappingOrder() {
        var first = new ClassNamingImpl()
                .replace("b.B", "x.Y")
                .replace("a.A", "y.X");
        var second = new ClassNamingImpl()
                .replace("a.A", "y.X")
                .replace("b.B", "x.Y");

        assertEquals(first.signature(), second.signature());
    }

    @Test
    void signatureChangesWithRules() {
        var origin = new ClassNamingImpl();
        var prefixed = new ClassNamingImpl().prefix("X");
        var suffixed = new ClassNamingImpl().suffix("Api");

        assertNotEquals(origin.signature(), prefixed.signature());
        assertNotEquals(prefixed.signature(), suffixed.signature());
        assertEquals("prefix=,suffix=,mapping={},packages=[],suffixes=[]", origin.signature());
    }
}
