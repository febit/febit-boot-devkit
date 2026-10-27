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
import org.junit.jupiter.api.Test;

import org.febit.boot.devkit.jooq.runtime.JooqGeneratorStrategy;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpiContextTest {

    @Test
    void contextDelegates() {
        var strategy = mock(JooqGeneratorStrategy.class);
        var def = mock(Definition.class);
        var impls = new ArrayList<String>();

        var context = new ImplementsResolver.ContextImpl(
                strategy, def, GeneratorStrategy.Mode.POJO, impls::add);

        assertSame(strategy, context.strategy());
        assertSame(def, context.def());
        assertEquals(GeneratorStrategy.Mode.POJO, context.mode());

        // deprecated aliases
        assertSame(strategy, context.getStrategy());
        assertSame(def, context.getDef());
        assertEquals(GeneratorStrategy.Mode.POJO, context.getMode());

        context.addImpl("org.demo.Impl");
        assertEquals(List.of("org.demo.Impl"), impls);
    }

    @Test
    void isTableDefinition() {
        var impls = new ArrayList<String>();
        var context = new ImplementsResolver.ContextImpl(
                mock(JooqGeneratorStrategy.class),
                mock(Definition.class),
                GeneratorStrategy.Mode.DEFAULT,
                impls::add
        );

        assertFalse(context.isTableDefinition());
    }

    @Test
    void resolveOutputName() {
        var strategy = mock(JooqGeneratorStrategy.class);
        var def = mock(Definition.class);
        when(strategy.resolveOutputName(def)).thenReturn("demo_name");

        var context = new ImplementsResolver.ContextImpl(
                strategy, def, GeneratorStrategy.Mode.DEFAULT, impl -> {
        });

        assertEquals("demo_name", context.resolveOutputName(def));
    }

    @Test
    void namingDefaults() {
        var naming = new Naming() {
        };
        var def = mock(Definition.class);

        assertNull(naming.memberField(def, GeneratorStrategy.Mode.POJO));
        assertNull(naming.identifier(def));
        assertNull(naming.getPackageName("org.demo", def, GeneratorStrategy.Mode.POJO));
        assertNull(naming.className(def));
    }
}
