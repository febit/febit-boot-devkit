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

import org.jooq.meta.ColumnDefinition;
import org.jooq.meta.DataTypeDefinition;
import org.jooq.meta.TableDefinition;
import org.jooq.meta.UniqueKeyDefinition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mocked jOOQ meta definitions shared by the tests.
 */
public final class JooqMetaFixtures {

    private JooqMetaFixtures() {
    }

    public static TableDefinition table(String outputName) {
        var table = mock(TableDefinition.class);
        when(table.getOutputName()).thenReturn(outputName);
        return table;
    }

    /**
     * A table with the given {@code column name -> column type} mapping.
     */
    public static TableDefinition table(Map<String, String> columns) {
        var table = mock(TableDefinition.class);
        new LinkedHashMap<>(columns).forEach((name, type) -> {
            var column = column(name, type);
            when(table.getColumn(name, true)).thenReturn(column);
        });
        return table;
    }

    public static TableDefinition tableWithPk(String outputName, ColumnDefinition... pkColumns) {
        var pk = mock(UniqueKeyDefinition.class);
        when(pk.getKeyColumns()).thenReturn(List.of(pkColumns));

        var table = table(outputName);
        when(table.getPrimaryKey()).thenReturn(pk);
        return table;
    }

    public static ColumnDefinition column(String outputName, String type) {
        var dataType = mock(DataTypeDefinition.class);
        when(dataType.getType()).thenReturn(type);

        var column = mock(ColumnDefinition.class);
        when(column.getOutputName()).thenReturn(outputName);
        when(column.getType()).thenReturn(dataType);
        return column;
    }
}
