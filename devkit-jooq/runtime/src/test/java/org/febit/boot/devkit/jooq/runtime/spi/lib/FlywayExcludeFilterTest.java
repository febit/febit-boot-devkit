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
package org.febit.boot.devkit.jooq.runtime.spi.lib;

import org.jooq.meta.TableDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FlywayExcludeFilterTest {

    final FlywayExcludeFilter filter = new FlywayExcludeFilter();

    @Test
    void excludeFlywayTables() {
        assertTrue(filter.exclude(table("flyway_schema_history")));
        assertTrue(filter.exclude(table("FLYWAY_SCHEMA_HISTORY")));
    }

    @Test
    void keepOtherTables() {
        assertFalse(filter.exclude(table("demo")));
        assertFalse(filter.exclude(table("not_flyway_history")));
    }

    private static TableDefinition table(String name) {
        var table = mock(TableDefinition.class);
        when(table.getName()).thenReturn(name);
        return table;
    }
}
