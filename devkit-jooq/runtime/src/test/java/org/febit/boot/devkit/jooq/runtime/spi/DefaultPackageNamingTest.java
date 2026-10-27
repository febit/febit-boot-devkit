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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class DefaultPackageNamingTest {

    static final String BASE = "org.febit.demo";

    final DefaultPackageNaming naming = new DefaultPackageNaming();

    @Test
    void packageNameByMode() {
        var def = mock(Definition.class);

        assertEquals(BASE + ".record", naming.getPackageName(BASE, def, GeneratorStrategy.Mode.RECORD));
        assertEquals(BASE + ".po", naming.getPackageName(BASE, def, GeneratorStrategy.Mode.POJO));
        assertEquals(BASE + ".dao", naming.getPackageName(BASE, def, GeneratorStrategy.Mode.DAO));
        assertEquals(BASE + ".interfaces", naming.getPackageName(BASE, def, GeneratorStrategy.Mode.INTERFACE));
    }

    @Test
    void defaultModeDependsOnDefinitionType() {
        assertEquals(BASE + ".table", naming.getPackageName(BASE, mock(TableDefinition.class),
                GeneratorStrategy.Mode.DEFAULT));
        assertNull(naming.getPackageName(BASE, mock(Definition.class),
                GeneratorStrategy.Mode.DEFAULT));
    }

    @Test
    void otherModesAreNotHandled() {
        assertNull(naming.getPackageName(BASE, mock(TableDefinition.class),
                GeneratorStrategy.Mode.ENUM));
    }
}
