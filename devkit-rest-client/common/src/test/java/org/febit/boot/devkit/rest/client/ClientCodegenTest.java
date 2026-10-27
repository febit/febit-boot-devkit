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
package org.febit.boot.devkit.rest.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.febit.boot.devkit.rest.client.fixture.LegacyController;
import org.febit.boot.devkit.rest.client.fixture.UserController;
import org.febit.boot.devkit.rest.client.fixture.UserInfo;
import org.febit.boot.devkit.rest.client.fixture.UserStatus;
import org.febit.boot.devkit.rest.client.meta.ApiDef;
import org.febit.boot.devkit.rest.client.meta.ApiParameterDef;
import org.febit.boot.devkit.rest.client.meta.ClientDef;
import org.febit.boot.devkit.rest.client.meta.RequestDef;
import org.febit.boot.devkit.rest.client.util.ClassNamingImpl;
import org.febit.boot.devkit.rest.client.util.ClassNamings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("deprecation")
class ClientCodegenTest {

    static final String BASE_PKG = "org.example.client";

    static final String FIXTURE_PKG = "org.febit.boot.devkit.rest.client.fixture";

    @TempDir
    Path targetDir;

    @Test
    void emitClient() throws IOException {
        emit(List.of(client(UserController.class, List.of(
                api("get", "/{id}",
                        ResolvableType.forClass(UserInfo.class),
                        ApiParameterDef.builder()
                                .name("id")
                                .in(ApiParameterDef.In.PATH_VAR)
                                .bindName("id")
                                .type(ResolvableType.forClass(long.class))
                                .build()
                ),
                api("list", "/list",
                        ResolvableType.forClassWithGenerics(List.class, UserInfo.class),
                        ApiParameterDef.builder()
                                .name("keyword")
                                .in(ApiParameterDef.In.QUERY)
                                .bindName("keyword")
                                .required(Boolean.FALSE)
                                .type(ResolvableType.forClass(String.class))
                                .build()
                )
        ))));

        var client = content("UserController");
        assertTrue(client.contains("package " + BASE_PKG + ";"));
        assertTrue(client.contains("@HttpExchange(\n        value = \"/users\""));
        assertTrue(client.contains("public interface UserController"));
        assertTrue(client.contains("@GetExchange("));
        assertTrue(client.contains("@PathVariable(\"id\") long id"));
        assertTrue(client.contains("@RequestParam(value = \"keyword\", required = false) String keyword"));
        assertTrue(client.contains("List<UserInfo> list("));

        // POJOs used by the client are emitted as well
        var pojo = content("UserInfo");
        assertTrue(pojo.contains("public class UserInfo"));
        assertTrue(pojo.contains("@lombok.Data"));
        assertTrue(pojo.contains("private String name;"));
        assertTrue(pojo.contains("@Deprecated"));
    }

    @Test
    void emitDeprecatedClient() throws IOException {
        emit(List.of(client(LegacyController.class, List.of(
                api("ping", "/ping",
                        ResolvableType.forClass(String.class))
        ))));

        var client = content("LegacyController");
        assertTrue(client.contains("@Deprecated\n@HttpExchange("));
    }

    @Test
    void emitEnum() throws IOException {
        emit(List.of(client(UserController.class, List.of(
                api("statuses", "/status",
                        ResolvableType.forClassWithGenerics(List.class, UserStatus.class))
        ))));

        var enumType = content("UserStatus");
        assertTrue(enumType.contains("public enum UserStatus"));
        assertTrue(enumType.contains("ACTIVE,"));
        assertTrue(enumType.contains("DISABLED,"));
    }

    @Test
    void emitParameterKinds() throws IOException {
        emit(List.of(client(UserController.class, List.of(
                api("update", "/update", ResolvableType.forClass(Void.class),
                        parameter("body", ApiParameterDef.In.BODY),
                        parameter("filter", ApiParameterDef.In.MODEL_ATTR),
                        parameter("form", ApiParameterDef.In.QUERY_MAP)
                )
        ))));

        var client = content("UserController");
        assertTrue(client.contains("@RequestBody Void body"));
        assertTrue(client.contains("@ModelAttribute Void filter"));
        assertTrue(client.contains("@RequestParamForm Void form"));
    }

    @Test
    void skipExcludedClasses() throws IOException {
        emit(List.of(client(UserController.class, List.of(
                api("get", "/{id}", ResolvableType.forClass(UserInfo.class))
        ))), Set.of(UserInfo.class.getName()));

        assertTrue(Files.exists(targetDir.resolve(BASE_PKG.replace('.', '/') + "/UserController.java")));
        assertFalse(Files.exists(targetDir.resolve(BASE_PKG.replace('.', '/') + "/UserInfo.java")));
    }

    private void emit(List<ClientDef> clients, Set<String> excludedClasses) {
        var naming = new ClassNamingImpl()
                .replacePackage(FIXTURE_PKG, BASE_PKG);

        ClientCodegen.builder()
                .beans(Map.of())
                .clients(clients)
                .clientBasePackage(BASE_PKG)
                .clientNaming(ClassNamings.chain(naming))
                .pojoNaming(ClassNamings.chain(naming))
                .excludedClasses(excludedClasses)
                .targetDir(targetDir.toFile())
                .build()
                .emit();
    }

    private void emit(List<ClientDef> clients) {
        emit(clients, Set.of());
    }

    private String content(String simpleName) throws IOException {
        var file = targetDir.resolve(BASE_PKG.replace('.', '/') + "/" + simpleName + ".java");
        assertTrue(Files.exists(file), "Missing generated file: " + file);
        return Files.readString(file);
    }

    private static ClientDef client(Class<?> type, List<ApiDef> apis) {
        var builder = ClientDef.builder()
                .type(type)
                .deprecated(type.isAnnotationPresent(Deprecated.class))
                .request(request(type));
        apis.forEach(builder::api);
        return builder.build();
    }

    private static ApiDef api(
            String name,
            String path,
            ResolvableType returnType,
            ApiParameterDef... parameters
    ) {
        var builder = ApiDef.builder()
                .name(name)
                .request(RequestDef.builder()
                        .paths(List.of(path))
                        .methods(List.of(RequestMethod.GET))
                        .build())
                .returnType(returnType);
        for (var parameter : parameters) {
            builder.parameter(parameter);
        }
        return builder.build();
    }

    private static ApiParameterDef parameter(String name, ApiParameterDef.In in) {
        return ApiParameterDef.builder()
                .name(name)
                .in(in)
                .type(ResolvableType.forClass(Void.class))
                .build();
    }

    private static RequestDef request(Class<?> type) {
        var mapping = AnnotatedElementUtils.findMergedAnnotation(type, RequestMapping.class);
        return RequestDef.of(mapping);
    }
}
