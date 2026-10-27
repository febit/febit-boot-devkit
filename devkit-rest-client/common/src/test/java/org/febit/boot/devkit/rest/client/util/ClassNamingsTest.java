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
package org.febit.boot.devkit.rest.client.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ClassNamingsTest {

    @Test
    void chainAppliesInOrder() {
        ClassNaming naming = ClassNamings.chain(
                origin -> "1:" + origin,
                origin -> "2:" + origin
        );

        assertEquals("2:1:demo", naming.resolve("demo"));
    }

    @Test
    void chainOfCollection() {
        ClassNaming naming = ClassNamings.chain(List.of(
                origin -> origin + ".A",
                origin -> origin + ".B"
        ));

        assertEquals("demo.A.B", naming.resolve("demo"));
    }

    @Test
    void cacheResolvesOnlyOnce() {
        var counter = new AtomicInteger();
        var naming = ClassNamings.cache(origin -> {
            counter.incrementAndGet();
            return origin + "!";
        });

        assertEquals("demo!", naming.resolve("demo"));
        assertEquals("demo!", naming.resolve("demo"));
        assertEquals(1, counter.get());
    }

    @Test
    void chainIsCached() {
        var counter = new AtomicInteger();
        ClassNaming naming = ClassNamings.chain(origin -> {
            counter.incrementAndGet();
            return origin;
        });

        assertSame("demo", naming.resolve("demo"));
        naming.resolve("demo");

        assertEquals(1, counter.get());
    }
}
