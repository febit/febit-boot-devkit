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

import org.febit.boot.devkit.rest.client.fixture.LegacyController;
import org.febit.boot.devkit.rest.client.fixture.UserController;
import org.febit.boot.devkit.rest.client.fixture.UserInfo;
import org.febit.boot.devkit.rest.client.meta.ApiDef;
import org.febit.boot.devkit.rest.client.meta.ApiParameterDef;
import org.febit.boot.devkit.rest.client.meta.ClientDef;
import org.febit.lang.protocol.IListResponse;
import org.febit.lang.protocol.IPageResponse;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("deprecation")
class MetaResolverTest {

    static final String PKG = "org.febit.boot.devkit.rest.client.fixture";

    @Test
    void resolveClients() {
        var clients = clients();

        assertEquals(2, clients.size());
        assertTrue(clients.stream().anyMatch(client -> client.type() == UserController.class));
        assertTrue(clients.stream().anyMatch(client -> client.type() == LegacyController.class));
    }

    @Test
    void resolveClient() {
        var client = clientOf(UserController.class);

        assertEquals(List.of("/users"), client.request().paths());
        assertFalse(client.deprecated());
        assertEquals(Set.of("get", "list", "page", "listResponse", "statuses"),
                apiNames(client));
    }

    @Test
    void resolveApiByPathVariables() {
        var api = apiOf(clientOf(UserController.class), "get");

        assertEquals("/{id}", api.request().getPrimaryPath());
        assertFalse(api.deprecated());
        assertEquals(UserInfo.class, api.returnType().resolve());

        var parameters = api.parameters();
        assertEquals(1, parameters.size());

        var id = parameters.getFirst();
        assertEquals("id", id.name());
        assertEquals("id", id.bindName());
        assertEquals(ApiParameterDef.In.PATH_VAR, id.in());
        assertTrue(id.required());
        assertEquals(long.class, id.type().resolve());
    }

    @Test
    void resolveApiWithGenerics() {
        var api = apiOf(clientOf(UserController.class), "list");

        assertEquals("/list", api.request().getPrimaryPath());
        assertEquals(List.class, api.returnType().resolve());

        var parameters = api.parameters();
        assertEquals(1, parameters.size());

        var keyword = parameters.getFirst();
        assertEquals("keyword", keyword.name());
        assertEquals(ApiParameterDef.In.QUERY, keyword.in());
        assertFalse(keyword.required());
    }

    @Test
    void resolveResponseType() {
        var client = clientOf(UserController.class);

        var list = apiOf(client, "list");
        assertEquals(List.class, list.returnType().resolve());

        // IResponse<List<T>> -> IListResponse<T>
        var listResponse = apiOf(client, "listResponse");
        assertEquals(IListResponse.class, listResponse.returnType().resolve());
        assertEquals(UserInfo.class, listResponse.returnType().getGeneric(0).resolve());

        // IResponse<Page<T>> -> IPageResponse<T>
        var page = apiOf(client, "page");
        assertEquals(IPageResponse.class, page.returnType().resolve());
        assertEquals(UserInfo.class, page.returnType().getGeneric(0).resolve());
    }

    @Test
    void resolveDeprecatedClient() {
        var client = clientOf(LegacyController.class);

        assertTrue(client.deprecated());
        assertEquals(1, client.apis().size());
        assertEquals("ping", client.apis().getFirst().name());
    }

    private static Set<String> apiNames(ClientDef client) {
        return client.apis().stream()
                .map(ApiDef::name)
                .collect(Collectors.toSet());
    }

    private static ApiDef apiOf(ClientDef client, String name) {
        return client.apis().stream()
                .filter(api -> name.equals(api.name()))
                .findFirst()
                .orElseThrow();
    }

    private static ClientDef clientOf(Class<?> type) {
        return clients().stream()
                .filter(client -> client.type() == type)
                .findFirst()
                .orElseThrow();
    }

    private static List<ClientDef> clients() {
        var resolver = MetaResolver.create(MetaResolverTest.class.getClassLoader());
        var beans = resolver.scanBeans(List.of(PKG));
        return resolver.resolveClients(beans.values());
    }
}
