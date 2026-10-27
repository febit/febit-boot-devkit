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
package org.febit.boot.devkit.jooq.meta.embedded;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageUtilsTest {

    @Test
    void os() {
        var os = PackageUtils.os();

        assertNotNull(os);
        assertFalse(os.getIdent().isBlank());
    }

    @Test
    void arch() {
        var arch = PackageUtils.arch();

        assertNotNull(arch);
        assertFalse(arch.getArtifact().isBlank());
    }

    @Test
    void postgresArtifact() {
        var artifact = PackageUtils.Postgres.artifact();

        assertTrue(artifact.startsWith("io.zonky.test.postgres:embedded-postgres-binaries-"));
        assertTrue(artifact.contains(PackageUtils.os().getIdent()));
        assertTrue(artifact.endsWith(PackageUtils.arch().getArtifact()));
    }
}
