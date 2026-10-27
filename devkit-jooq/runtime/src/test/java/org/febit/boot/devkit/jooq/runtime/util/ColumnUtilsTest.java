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
package org.febit.boot.devkit.jooq.runtime.util;

import org.jooq.meta.ColumnDefinition;
import org.junit.jupiter.api.Test;

import org.febit.boot.devkit.jooq.runtime.JooqMetaFixtures;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColumnUtilsTest {

    @Test
    void type() {
        assertEquals(ColumnUtils.TYPE_INTEGER, type("int"));
        assertEquals(ColumnUtils.TYPE_INTEGER, type("integer"));
        assertEquals(ColumnUtils.TYPE_INTEGER, type("INT"));
        assertEquals(ColumnUtils.TYPE_LONG, type("bigint"));
        assertEquals(ColumnUtils.TYPE_LONG, type("long"));
        assertEquals(ColumnUtils.TYPE_INSTANT, type("timestamp"));
        assertEquals(ColumnUtils.TYPE_INSTANT, type("timestamptz"));
        assertEquals(ColumnUtils.TYPE_INSTANT, type("timestamp with time zone"));
        assertEquals(ColumnUtils.TYPE_STRING, type("varchar(32)"));
        assertEquals(ColumnUtils.TYPE_STRING, type("varchar_ignorecase"));
        assertEquals(ColumnUtils.TYPE_STRING, type("character varying"));
        assertEquals(ColumnUtils.TYPE_UUID, type("uuid"));
    }

    @Test
    void typeFallback() {
        assertEquals("numeric(10,2)", type("numeric(10,2)"));
        assertEquals("blob", type("blob"));
    }

    @Test
    void predicates() {
        assertTrue(ColumnUtils.isInteger(column("int")));
        assertTrue(ColumnUtils.isLong(column("bigint")));
        assertTrue(ColumnUtils.isString(column("varchar(64)")));
        assertTrue(ColumnUtils.isInstant(column("timestamp")));
        assertFalse(ColumnUtils.isString(column("int")));
    }

    @Test
    void checkTableColumn() {
        var table = JooqMetaFixtures.table(Map.of("id", "int"));

        assertTrue(ColumnUtils.isInteger(table, "id"));
        assertFalse(ColumnUtils.isInteger(table, "missing"));
        assertFalse(ColumnUtils.isString(table, "id"));
    }

    private static String type(String type) {
        return ColumnUtils.type(column(type));
    }

    private static ColumnDefinition column(String type) {
        return JooqMetaFixtures.column("demo_column", type);
    }
}
