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

import org.febit.boot.devkit.jooq.runtime.JooqGeneratorStrategy;
import org.febit.boot.devkit.jooq.runtime.JooqMetaFixtures;
import org.febit.boot.devkit.jooq.runtime.spi.lib.BootImplsResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class BootImplsResolverTest {

    static final String CLS_NAMED = "org.febit.boot.model.INamed";
    static final String CLS_AUDITABLE = "org.febit.boot.model.IAuditable";

    final BootImplsResolver resolver = new BootImplsResolver();

    @Test
    void namedAndAuditable() {
        var table = JooqMetaFixtures.table(Map.of(
                "name", "varchar(64)",
                "created_at", "timestamp",
                "updated_at", "timestamp",
                "created_by", "varchar(64)",
                "updated_by", "varchar(64)"
        ));

        assertEquals(List.of(CLS_NAMED, CLS_AUDITABLE),
                resolve(table, GeneratorStrategy.Mode.POJO));
    }

    @Test
    void namedOnly() {
        var table = JooqMetaFixtures.table(Map.of("name", "varchar(64)"));

        assertEquals(List.of(CLS_NAMED), resolve(table, GeneratorStrategy.Mode.POJO));
    }

    @Test
    void auditableRequiresAllColumns() {
        var table = JooqMetaFixtures.table(Map.of(
                "created_at", "timestamp",
                "updated_at", "timestamp",
                "created_by", "varchar(64)"
                // updated_by is missing
        ));

        assertTrue(resolve(table, GeneratorStrategy.Mode.POJO).isEmpty());
    }

    @Test
    void noMatchedColumns() {
        var table = JooqMetaFixtures.table(Map.of("title", "varchar(64)"));

        assertTrue(resolve(table, GeneratorStrategy.Mode.POJO).isEmpty());
    }

    @Test
    void otherModesAreIgnored() {
        var table = JooqMetaFixtures.table(Map.of("name", "varchar(64)"));

        assertTrue(resolve(table, GeneratorStrategy.Mode.RECORD).isEmpty());
        assertTrue(resolve(table, GeneratorStrategy.Mode.DEFAULT).isEmpty());
    }

    @Test
    void nonTableDefinitionIsIgnored() {
        var impls = new ArrayList<String>();
        resolver.resolve(new ImplementsResolver.ContextImpl(
                mock(JooqGeneratorStrategy.class),
                mock(Definition.class),
                GeneratorStrategy.Mode.POJO,
                impls::add
        ));

        assertTrue(impls.isEmpty());
    }

    private List<String> resolve(TableDefinition table, GeneratorStrategy.Mode mode) {
        var impls = new ArrayList<String>();
        resolver.resolve(new ImplementsResolver.ContextImpl(
                mock(JooqGeneratorStrategy.class),
                table,
                mode,
                impls::add
        ));
        return impls;
    }
}
