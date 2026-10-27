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
package org.febit.boot.devkit.jooq.runtime.spi;

import org.jooq.codegen.GeneratorStrategy;
import org.jooq.meta.Definition;
import org.jooq.meta.TableDefinition;
import org.junit.jupiter.api.Test;

import org.febit.boot.devkit.jooq.meta.MetaUtils;
import org.febit.boot.devkit.jooq.runtime.JooqGeneratorStrategy;
import org.febit.boot.devkit.jooq.runtime.JooqMetaFixtures;
import org.febit.boot.devkit.jooq.runtime.util.ColumnUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BasicImplementsResolverTest {

    final BasicImplementsResolver resolver = new BasicImplementsResolver();

    @Test
    void tableInterface() {
        var strategy = strategy();

        assertEquals(List.of(MetaUtils.CORE_PKG + ".ITable<DemoRecord, Integer>"),
                resolve(strategy, tableWithPk(), GeneratorStrategy.Mode.DEFAULT));
    }

    @Test
    void entityInterface() {
        var strategy = strategy();

        assertEquals(List.of(MetaUtils.CORE_PKG + ".IEntity<Integer>"),
                resolve(strategy, tableWithPk(), GeneratorStrategy.Mode.POJO));
    }

    @Test
    void otherModesAreIgnored() {
        var strategy = strategy();

        assertTrue(resolve(strategy, tableWithPk(), GeneratorStrategy.Mode.RECORD).isEmpty());
    }

    @Test
    void noPrimaryKey() {
        assertTrue(resolve(strategy(), JooqMetaFixtures.table("demo"),
                GeneratorStrategy.Mode.DEFAULT).isEmpty());
    }

    @Test
    void compositePrimaryKey() {
        var table = JooqMetaFixtures.tableWithPk("demo",
                JooqMetaFixtures.column("id", "int"),
                JooqMetaFixtures.column("version", "int")
        );

        assertTrue(resolve(strategy(), table, GeneratorStrategy.Mode.DEFAULT).isEmpty());
    }

    @Test
    void nonTableDefinitionIsIgnored() {
        var impls = new ArrayList<String>();
        resolver.resolve(new ImplementsResolver.ContextImpl(
                strategy(),
                mock(Definition.class),
                GeneratorStrategy.Mode.DEFAULT,
                impls::add
        ));

        assertTrue(impls.isEmpty());
    }

    private static TableDefinition tableWithPk() {
        return JooqMetaFixtures.tableWithPk("demo",
                JooqMetaFixtures.column("id", "int"));
    }

    private List<String> resolve(
            JooqGeneratorStrategy strategy,
            TableDefinition table,
            GeneratorStrategy.Mode mode
    ) {
        var impls = new ArrayList<String>();
        resolver.resolve(new ImplementsResolver.ContextImpl(
                strategy,
                table,
                mode,
                impls::add
        ));
        return impls;
    }

    private static JooqGeneratorStrategy strategy() {
        var strategy = mock(JooqGeneratorStrategy.class);
        when(strategy.resolveColumnType(any())).thenReturn(ColumnUtils.TYPE_INTEGER);
        when(strategy.getJavaClassName(any(), any())).thenReturn("DemoRecord");
        return strategy;
    }
}
