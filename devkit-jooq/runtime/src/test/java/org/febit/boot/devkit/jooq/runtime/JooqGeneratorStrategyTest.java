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
package org.febit.boot.devkit.jooq.runtime;

import org.jooq.codegen.GeneratorStrategy;
import org.jooq.meta.CatalogDefinition;
import org.jooq.meta.Definition;
import org.jooq.meta.SchemaDefinition;
import org.junit.jupiter.api.Test;

import org.febit.boot.devkit.jooq.runtime.util.ColumnUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JooqGeneratorStrategyTest {

    final JooqGeneratorStrategy strategy = new JooqGeneratorStrategy();

    @Test
    void resolveOutputName() {
        var def = definition("user_name");

        assertEquals("user_name", strategy.resolveOutputName(def));
    }

    @Test
    void resolveOutputNameIsRequired() {
        var def = definition(null);

        assertThrows(NullPointerException.class, () -> strategy.resolveOutputName(def));
    }

    @Test
    void resolveColumnType() {
        assertEquals(ColumnUtils.TYPE_LONG, strategy.resolveColumnType(JooqMetaFixtures.column("demo_col", "bigint")));
    }

    @Test
    void javaIdentifier() {
        assertEquals("USER_NAME", strategy.getJavaIdentifier(JooqMetaFixtures.table("user_name")));
        assertEquals("USER_NAME", strategy.getJavaIdentifier(JooqMetaFixtures.column("user_name", "int")));
    }

    @Test
    void javaMemberName() {
        assertEquals("userName", strategy.getJavaMemberName(
                JooqMetaFixtures.column("user_name", "int"), GeneratorStrategy.Mode.POJO));
    }

    @Test
    void javaClassName() {
        assertEquals("UserNameRecord", strategy.getJavaClassName(
                JooqMetaFixtures.table("user_name"), GeneratorStrategy.Mode.RECORD));
        assertEquals("UserNamePO", strategy.getJavaClassName(
                JooqMetaFixtures.table("user_name"), GeneratorStrategy.Mode.POJO));
        assertEquals("IUserName", strategy.getJavaClassName(
                JooqMetaFixtures.table("user_name"), GeneratorStrategy.Mode.INTERFACE));
        assertEquals("TUserName", strategy.getJavaClassName(
                JooqMetaFixtures.table("user_name"), GeneratorStrategy.Mode.DEFAULT));
    }

    @Test
    void defaultCatalogAndSchema() {
        var catalog = mock(CatalogDefinition.class);
        when(catalog.isDefaultCatalog()).thenReturn(true);
        var schema = mock(SchemaDefinition.class);
        when(schema.isDefaultSchema()).thenReturn(true);

        assertEquals("DefaultCatalog",
                strategy.getJavaClassName(catalog, GeneratorStrategy.Mode.DEFAULT));
        assertEquals("DefaultSchema",
                strategy.getJavaClassName(schema, GeneratorStrategy.Mode.DEFAULT));
    }

    @Test
    void javaClassImplements() {
        var table = JooqMetaFixtures.tableWithPk("demo",
                JooqMetaFixtures.column("id", "int"));

        var impls = strategy.getJavaClassImplements(table, GeneratorStrategy.Mode.POJO);

        assertTrue(impls.contains("org.febit.common.jooq.IEntity<Integer>"));
    }

    @Test
    void pkColumn() {
        var column = JooqMetaFixtures.column("id", "int");
        var table = JooqMetaFixtures.tableWithPk("demo", column);

        assertEquals(column, JooqGeneratorStrategy.getPkColumn(table));
        assertNull(JooqGeneratorStrategy.getPkColumn(JooqMetaFixtures.table("no_pk")));
    }

    private static Definition definition(String outputName) {
        var def = mock(Definition.class);
        when(def.getOutputName()).thenReturn(outputName);
        return def;
    }
}
