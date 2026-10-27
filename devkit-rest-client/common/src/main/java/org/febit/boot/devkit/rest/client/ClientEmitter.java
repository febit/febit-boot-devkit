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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import org.febit.boot.devkit.rest.client.meta.ApiDef;
import org.febit.boot.devkit.rest.client.meta.ApiParameterDef;
import org.febit.boot.devkit.rest.client.meta.ClientDef;
import org.febit.boot.devkit.rest.client.util.ClassWriter;
import org.febit.boot.devkit.rest.client.util.ImportSet;
import org.febit.devkit.gradle.util.JavaUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
class ClientEmitter {

    private static final List<String> DEFAULT_IMPORTS = List.of(
            "org.febit.lang.protocol.*",
            "org.febit.common.rest.client.service.annotation.*",
            "org.springframework.web.bind.annotation.*",
            "org.springframework.web.service.annotation.*"
    );

    private final ClassWriter out;
    private final ImportSet imports;

    private final ClientCodegen codegen;
    private final ClientDef client;

    private final String clientPath;
    private final String apiPathPrefix;

    ClientEmitter(ClientCodegen codegen, ClientDef client) {
        this.codegen = codegen;
        this.client = client;

        var fullName = codegen.clientNaming.resolve(client.type().getCanonicalName());
        this.out = ClassWriter.create(codegen.pojoNaming, fullName);
        this.imports = this.out.getImports();

        DEFAULT_IMPORTS.forEach(this.imports::add);

        var path = client.request().getPrimaryPath();
        // NOTE: Path-Variables are not support in client-level settings.
        this.clientPath = StringUtils.substringBefore(path, "/{");
        this.apiPathPrefix = Strings.CS.removeEnd(
                path.substring(this.clientPath.length()),
                "/"
        );
    }

    void emit() {
        log.info("Emit client: {}", this.out.getFullName());

        // Imports
        client.apis().forEach(api -> {
            imports.add(api.returnType());
            api.parameters()
                    .stream()
                    .map(ApiParameterDef::type)
                    .forEach(imports::add);
        });

        // Emit POJOs
        client.apis().forEach(api -> {
            codegen.emitPojoIfAbsent(api.returnType());
            api.parameters()
                    .stream()
                    .map(ApiParameterDef::type)
                    .forEach(codegen::emitPojoIfAbsent);
        });

        out.appendClassHeader();

        if (Boolean.TRUE.equals(client.deprecated())) {
            out.append("@Deprecated\n");
        }

        emitClientConfigs();
        out.appendDeclare("interface");

        client.apis()
                .forEach(this::emitApi);

        out.closeAndSinkTo(codegen.targetDir);
    }

    private void emitClientConfigs() {

        out.append("@HttpExchange(\n")
                .tab(2).append("value = ").appendDirectValue(clientPath).append(",\n")
                .tab(2).append("accept = \"application/json\"")
                .append("\n)\n");
    }

    private void emitApi(ApiDef api) {
        out.append('\n');

        if (Boolean.TRUE.equals(api.deprecated())) {
            out.tab(1).append("@Deprecated\n");
        }

        emitApiRequestMapping(api);

        out.tab(1).append("")
                .appendType(api.returnType())
                .append(" ")
                .append(api.name())
                .append('(');

        var params = api.parameters();
        if (!params.isEmpty()) {
            out.append('\n');
            out.foreach(params, ",\n", "\n", this::emitApiParameter);
            out.tab(1);
        }

        out.append(");\n");
    }

    private void emitApiRequestMapping(ApiDef api) {
        var req = api.request();

        out.tab(1).append("");
        var method = req.method();
        out.append("@")
                .append(JavaUtils.upperFirst(method.name().toLowerCase()))
                .append("Exchange(\n");

        String path = req.getPrimaryPath();
        if (!path.isEmpty()) {
            path = Strings.CS.prependIfMissing(path, "/");
        }

        out.tab(2).append("url = ")
                .appendDirectValue(this.apiPathPrefix + path);

        if (!req.headers().isEmpty()) {
            out.append(",\n");
            out.tab(2).append("headers = {\n");
            out.foreach(req.headers(), ",\n", "\n", str ->
                    out.tab(3).appendDirectValue(str)
            );
            out.tab(2).append("}");
        }

        if (!req.consumes().isEmpty()) {
            out.append(",\n");
            out.tab(2).append("contentType = ").appendDirectValue(req.consumes().get(0));
        }

        if (!req.produces().isEmpty()) {
            out.append(",\n");
            out.tab(2).append("accept = {\n");
            out.foreach(req.produces(), ",\n", "\n", str ->
                    out.tab(3).appendDirectValue(str)
            );
            out.tab(2).append("}");
        }

        out.append('\n');

        // Close annotation
        out.tab(1).append(")\n");
    }

    private void emitApiParameter(ApiParameterDef param) {
        out.tab(3);

        var bindName = StringUtils.isNotEmpty(param.bindName())
                ? param.bindName()
                : param.name();

        if (Boolean.TRUE.equals(param.deprecated())) {
            out.append("@Deprecated ");
        }

        switch (param.in()) {
            case QUERY:
                out.append("@RequestParam(");
                emitBindArgs(bindName, param.required());
                out.append(") ");
                break;
            case QUERY_MAP:
                out.append("@RequestParamForm ");
                break;
            case PATH_VAR:
                out.append("@PathVariable(");
                emitBindArgs(bindName, param.required());
                out.append(") ");
                break;
            case HEADER:
                out.append("@RequestHeader(");
                emitBindArgs(bindName, param.required());
                out.append(") ");
                break;
            case MODEL_ATTR:
                out.append("@ModelAttribute ");
                break;
            case BODY:
                out.append("@RequestBody ");
                break;
            default:
                throw new IllegalArgumentException("Unsupported Parameter.In: " + param.in());
        }
        out.appendType(param.type())
                .append(' ')
                .append(param.name());
    }

    private void emitBindArgs(String bindName, Boolean required) {
        if (Boolean.FALSE.equals(required)) {
            out.append("value = ")
                    .appendDirectValue(bindName)
                    .append(", required = false");
        } else {
            out.appendDirectValue(bindName);
        }
    }
}
