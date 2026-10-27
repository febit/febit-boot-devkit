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
import org.springframework.core.ResolvableType;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportSetTest {

    final ImportSet imports = new ImportSet(new ClassNamingImpl());

    @Test
    void emitSortedAndFiltered() {
        imports.add("org.b.B");
        imports.add("org.a.A");
        imports.add("java.lang.String");
        imports.add("org.current.C");

        var buf = new StringBuilder();
        imports.emit(buf, "org.current");

        assertEquals("import org.a.A;\nimport org.b.B;\n\n", buf.toString());
    }

    @Test
    void emitEmpty() {
        var buf = new StringBuilder();
        imports.emit(buf, "org.current");

        assertEquals("", buf.toString());
    }

    @Test
    void addIgnoresPrimitives() {
        imports.add(int.class);

        var buf = new StringBuilder();
        imports.emit(buf, "org.current");

        assertEquals("", buf.toString());
    }

    @Test
    void addFlattensArrays() {
        imports.add(Date[].class);

        assertTrue(imports.contains(Date.class));
    }

    @Test
    void addResolvedTypeWithGenerics() {
        var type = ResolvableType.forClassWithGenerics(Map.class, String.class, Date.class);

        imports.add(type);

        assertTrue(imports.contains(Map.class));
        assertTrue(imports.contains(String.class));
        assertTrue(imports.contains(Date.class));
    }

    @Test
    void transferByNaming() {
        var naming = new ClassNamingImpl()
                .replacePackage("org.a", "org.x");
        var named = new ImportSet(naming);

        named.add("org.a.A");

        assertTrue(named.contains("org.a.A"));
        assertEquals("org.x.A", named.transfer("org.a.A"));
    }

    @Test
    void resolveClassEmitName() {
        imports.add("org.a.A");

        assertEquals("A", imports.resolveClassEmitName("org.a.A"));
        assertEquals("org.b.B", imports.resolveClassEmitName("org.b.B"));
        assertEquals("Date[]", resolveEmitName(Date[].class));
    }

    @Test
    void resolveTypeEmitName() {
        var type = ResolvableType.forClassWithGenerics(List.class, String.class);

        imports.add(type);

        assertEquals("List<String>", imports.resolveTypeEmitName(type));
    }

    @Test
    void resolveTypeEmitNameOfArray() {
        var type = ResolvableType.forClass(Date[].class);

        imports.add(type);

        assertEquals("Date[]", imports.resolveTypeEmitName(type));
    }

    @Test
    void unimportedTypeKeepsFullName() {
        assertFalse(imports.contains(Date.class));
        assertEquals("java.util.Date", imports.resolveClassEmitName(Date.class));
    }

    @Test
    void resolveTypeEmitNameNestedGenerics() {
        var type = ResolvableType.forClassWithGenerics(Map.class,
                ResolvableType.forClass(String.class),
                ResolvableType.forClassWithGenerics(List.class, Date.class));

        imports.add(type);

        assertEquals("Map<String, List<Date>>", imports.resolveTypeEmitName(type));
    }

    @Test
    void resolveTypeEmitNames() {
        var date = ResolvableType.forClass(Date.class);
        var string = ResolvableType.forClass(String.class);
        imports.add(date);
        imports.add(string);

        assertEquals(List.of("Date", "String"), imports.resolveTypeEmitNames(date, string));
    }

    /**
     * Raw names, e.g. the wildcard defaults {@code org.febit.lang.protocol.*},
     * are added as they are.
     */
    @Test
    void addStringIsRaw() {
        var naming = new ClassNamingImpl()
                .replacePackage("org.a", "org.x");
        var named = new ImportSet(naming);
        named.add("org.a.A");

        var buf = new StringBuilder();
        named.emit(buf, "org.current");

        assertEquals("import org.a.A;\n\n", buf.toString());
    }

    @Test
    void addClassIsTransferred() {
        var naming = new ClassNamingImpl()
                .replacePackage("java.util", "org.x");
        var named = new ImportSet(naming);

        named.add(Date.class);

        assertTrue(named.contains("org.x.Date"));

        var buf = new StringBuilder();
        named.emit(buf, "org.current");

        assertEquals("import org.x.Date;\n\n", buf.toString());
    }

    private String resolveEmitName(Class<?> cls) {
        imports.add(cls);
        return imports.resolveClassEmitName(cls);
    }
}
