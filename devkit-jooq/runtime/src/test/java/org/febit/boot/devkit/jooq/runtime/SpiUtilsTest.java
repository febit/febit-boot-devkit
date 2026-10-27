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

import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.Order;

import org.febit.boot.devkit.jooq.runtime.spi.ClassNameDecorator;
import org.febit.boot.devkit.jooq.runtime.spi.DefaultClassNameDecorator;
import org.febit.boot.devkit.jooq.runtime.spi.DefaultNaming;
import org.febit.boot.devkit.jooq.runtime.spi.DefaultPackageNaming;
import org.febit.boot.devkit.jooq.runtime.spi.ImplementsResolver;
import org.febit.boot.devkit.jooq.runtime.spi.Naming;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SpiUtilsTest {

    @Test
    void loadNaming() {
        var impls = SpiUtils.load(Naming.class);

        assertEquals(2, impls.size());
        assertInstanceOf(DefaultNaming.class, impls.get(0));
        assertInstanceOf(DefaultPackageNaming.class, impls.get(1));
    }

    @Test
    void loadClassNameDecorator() {
        var impls = SpiUtils.load(ClassNameDecorator.class);

        assertEquals(1, impls.size());
        assertInstanceOf(DefaultClassNameDecorator.class, impls.get(0));
    }

    @Test
    void loadImplementsResolver() {
        var impls = SpiUtils.load(ImplementsResolver.class);

        assertEquals(1, impls.size());
        assertEquals("BasicImplementsResolver", impls.get(0).getClass().getSimpleName());
    }

    @Test
    void sortByOrderAnnotation() {
        var list = new ArrayList<>(List.of(
                new SecondOrder(),
                new NoOrder(),
                new FirstOrder()
        ));

        list.sort(new SpiUtils.SpiOrderComparator());

        assertInstanceOf(NoOrder.class, list.get(0));
        assertInstanceOf(FirstOrder.class, list.get(1));
        assertInstanceOf(SecondOrder.class, list.get(2));
    }

    /**
     * No {@link Order} annotation, treated as normal priority(0).
     */
    static class NoOrder {
    }

    @Order(1)
    static class FirstOrder {
    }

    @Order(2)
    static class SecondOrder {
    }
}
