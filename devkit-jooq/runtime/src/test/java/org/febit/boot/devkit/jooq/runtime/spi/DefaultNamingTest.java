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
import org.jooq.meta.ColumnDefinition;
import org.jooq.meta.Definition;
import org.jooq.meta.TableDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.febit.boot.devkit.jooq.runtime.JooqGeneratorStrategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultNamingTest {

    final DefaultNaming naming = new DefaultNaming();

    @BeforeEach
    void setUp() {
        var strategy = mock(JooqGeneratorStrategy.class);
        when(strategy.resolveOutputName(any()))
                .thenAnswer(invocation -> ((Definition) invocation.getArgument(0)).getOutputName());
        naming.setGeneratorStrategy(strategy);
    }

    @Test
    void memberFieldOfColumn() {
        assertEquals("userName", naming.memberField(
                column("user_name"), GeneratorStrategy.Mode.POJO));
    }

    @Test
    void memberFieldOfOtherDefinition() {
        assertNull(naming.memberField(
                mock(Definition.class), GeneratorStrategy.Mode.POJO));
    }

    @Test
    void identifier() {
        assertEquals("USER_NAME", naming.identifier(table("user_name")));
        assertEquals("USER_NAME", naming.identifier(column("user_name")));
        assertNull(naming.identifier(mock(Definition.class)));
    }

    static TableDefinition table(String outputName) {
        var table = mock(TableDefinition.class);
        when(table.getOutputName()).thenReturn(outputName);
        return table;
    }

    static ColumnDefinition column(String outputName) {
        var column = mock(ColumnDefinition.class);
        when(column.getOutputName()).thenReturn(outputName);
        return column;
    }
}
