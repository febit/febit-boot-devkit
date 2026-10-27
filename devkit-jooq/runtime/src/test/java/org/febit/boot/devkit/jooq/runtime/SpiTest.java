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

import org.febit.boot.devkit.jooq.runtime.spi.OutputNameResolver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SpiTest {

    @Test
    void firstNonNullResultWins() {
        var spi = Spi.of(List.<OutputNameResolver>of(
                definition -> null,
                definition -> "second",
                definition -> "third"
        ));

        assertEquals("second", spi.compute(resolver -> resolver.resolve(null)));
    }

    @Test
    void defaultSupplier() {
        var spi = Spi.of(List.<OutputNameResolver>of(definition -> null));

        assertEquals("fallback", spi.compute(
                resolver -> resolver.resolve(null),
                () -> "fallback"
        ));
        assertNull(spi.compute(resolver -> resolver.resolve(null)));
    }

    @Test
    void emptySpi() {
        var spi = Spi.of(List.<OutputNameResolver>of());

        assertNull(spi.compute(resolver -> resolver.resolve(null)));
        assertEquals("fallback", spi.compute(
                resolver -> resolver.resolve(null),
                () -> "fallback"
        ));
    }
}
