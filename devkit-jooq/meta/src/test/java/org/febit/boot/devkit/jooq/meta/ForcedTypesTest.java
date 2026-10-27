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
package org.febit.boot.devkit.jooq.meta;

import org.jooq.meta.jaxb.ForcedType;
import org.junit.jupiter.api.Test;

import org.febit.lang.modeler.Schema;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForcedTypesTest {

    static final String EXPR = ".*\\.demo\\.status";

    @Test
    void typePatterns() {

        var regex = Pattern.compile(ForcedTypes.TypePatterns.STRING);

        assertTrue(regex.matcher("char").matches());
        assertTrue(regex.matcher("varchar").matches());
        assertTrue(regex.matcher("text").matches());
        assertTrue(regex.matcher("String").matches());
        assertTrue(regex.matcher("CHAR").matches());
        assertTrue(regex.matcher("character varying").matches());
        assertTrue(regex.matcher("character VARYING").matches());
    }

    @Test
    void timeToInstant() {
        var types = new ArrayList<ForcedType>();
        ForcedTypes.timeToInstant(types::add);

        assertEquals(2, types.size());

        var localDateTime = types.get(0);
        assertEquals(ForcedTypes.Converters.LOCAL_DATE_TIME_INSTANT, localDateTime.getConverter());
        assertEquals(ForcedTypes.EXPR_ALL, localDateTime.getIncludeExpression());
        assertEquals(Instant.class.getName(), localDateTime.getUserType());
        assertEquals(ForcedTypes.TypePatterns.DATETIME_OR_TIMESTAMP,
                localDateTime.getIncludeTypes());

        var offsetDateTime = types.get(1);
        assertEquals(ForcedTypes.Converters.OFFSET_DATE_TIME_INSTANT, offsetDateTime.getConverter());
        assertEquals(ForcedTypes.EXPR_ALL, offsetDateTime.getIncludeExpression());
        assertEquals(Instant.class.getName(), offsetDateTime.getUserType());
        assertEquals(ForcedTypes.TypePatterns.TIMESTAMP_TZ, offsetDateTime.getIncludeTypes());
    }

    @Test
    void timeToInstantWithExpression() {
        var types = new ArrayList<ForcedType>();
        ForcedTypes.timeToInstant(types::add, EXPR);

        assertEquals(EXPR, types.get(0).getIncludeExpression());
        // The timestamptz rule always applies to all columns
        assertEquals(ForcedTypes.EXPR_ALL, types.get(1).getIncludeExpression());
    }

    @Test
    void toBoolean() {
        var types = to("boolean");

        assertEquals(1, types.size());
        assertEquals("boolean", types.get(0).getName());
        assertEquals(EXPR, types.get(0).getIncludeExpression());
        assertEquals(ForcedTypes.TypePatterns.ANY, types.get(0).getIncludeTypes());
    }

    @Test
    void toEnum() {
        var types = to("enum:org.demo.FooStatus");

        assertEquals(1, types.size());
        assertEquals("org.demo.FooStatus", types.get(0).getUserType());
        assertEquals(ForcedTypes.Converters.VALUED_ENUM + ".forEnum(org.demo.FooStatus.class)",
                types.get(0).getConverter());
        assertEquals(ForcedTypes.TypePatterns.ANY, types.get(0).getIncludeTypes());
    }

    @Test
    void toJsonBean() {
        var types = to("json:org.demo.JsonBean");

        // jsonb / json / string columns
        assertEquals(3, types.size());

        assertEquals(ForcedTypes.TypePatterns.JSONB, types.get(0).getIncludeTypes());
        assertEquals(ForcedTypes.Converters.JSONB + ".forBean(org.demo.JsonBean.class)",
                types.get(0).getConverter());

        assertEquals(ForcedTypes.TypePatterns.JSON, types.get(1).getIncludeTypes());
        assertEquals(ForcedTypes.Converters.JSON + ".forBean(org.demo.JsonBean.class)",
                types.get(1).getConverter());

        assertEquals(ForcedTypes.TypePatterns.STRING, types.get(2).getIncludeTypes());
        assertEquals(ForcedTypes.Converters.JSON_STRING + ".forBean(org.demo.JsonBean.class)",
                types.get(2).getConverter());

        types.forEach(type -> {
            assertEquals("org.demo.JsonBean", type.getUserType());
            assertEquals(EXPR, type.getIncludeExpression());
        });
    }

    @Test
    void toJsonBeanArray() {
        var types = to("json:array:org.demo.JsonBean");

        assertEquals(3, types.size());
        assertEquals("org.demo.JsonBean[]", types.get(0).getUserType());
        assertEquals(ForcedTypes.Converters.JSONB + ".forBeanArray(org.demo.JsonBean.class)",
                types.get(0).getConverter());
    }

    @Test
    void toJsonBeanList() {
        var types = to("json:list:org.demo.JsonBean");

        assertEquals(3, types.size());
        assertEquals("java.util.List<org.demo.JsonBean>", types.get(0).getUserType());
        assertEquals(ForcedTypes.Converters.JSONB + ".forBeanList(org.demo.JsonBean.class)",
                types.get(0).getConverter());
    }

    @Test
    void toJsonBeanMap() {
        var types = to("json:map<int,org.demo.JsonBean>");

        assertEquals(3, types.size());
        assertEquals("java.util.Map<Integer, org.demo.JsonBean>", types.get(0).getUserType());
        assertEquals(ForcedTypes.Converters.JSONB
                        + ".forBeanMap(Integer.class, org.demo.JsonBean.class)",
                types.get(0).getConverter());
    }

    @Test
    void unsupportedTypes() {
        assertThrows(IllegalArgumentException.class, () -> to("int"));
        assertThrows(IllegalArgumentException.class, () -> to("string"));
    }

    private static List<ForcedType> to(String schema) {
        var types = new ArrayList<ForcedType>();
        ForcedTypes.to(types::add, EXPR, Schema.parse(schema));
        return types;
    }
}
