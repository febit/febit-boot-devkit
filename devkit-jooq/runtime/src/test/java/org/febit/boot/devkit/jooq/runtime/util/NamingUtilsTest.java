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
package org.febit.boot.devkit.jooq.runtime.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NamingUtilsTest {

    @Test
    void toUpperCamelCase() {
        assertEquals("FooBar", NamingUtils.toUpperCamelCase("foo_bar"));
        assertEquals("FooBar", NamingUtils.toUpperCamelCase("foo-bar"));
        assertEquals("FooBar", NamingUtils.toUpperCamelCase("foo.bar"));
        assertEquals("FooBar", NamingUtils.toUpperCamelCase("foo bar"));
        assertEquals("AbcDef", NamingUtils.toUpperCamelCase("ABC_DEF"));
    }

    @Test
    void toLowerCamelCase() {
        assertEquals("fooBar", NamingUtils.toLowerCamelCase("foo_bar"));
        assertEquals("createdAt", NamingUtils.toLowerCamelCase("created-at"));
        assertEquals("id", NamingUtils.toLowerCamelCase("ID"));
        assertEquals("urlPath", NamingUtils.toLowerCamelCase("url_path"));
    }

    @Test
    void firstChar() {
        assertEquals("userName", NamingUtils.lowerFirst("UserName"));
        assertEquals("UserName", NamingUtils.upperFirst("userName"));
        assertEquals("", NamingUtils.lowerFirst(""));
    }
}
