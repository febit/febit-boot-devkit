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
package org.febit.boot.devkit.rest.client.meta;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.febit.boot.devkit.rest.client.util.Immutables;
import org.febit.lang.util.Lists;

import java.util.List;

@lombok.Builder(builderClassName = "Builder")
public record RequestDef(
        List<String> paths,
        List<RequestMethod> methods,
        List<String> headers,
        List<String> consumes,
        List<String> produces
) {

    public RequestDef {
        paths = Immutables.copyOf(paths);
        methods = Immutables.copyOf(methods);
        headers = Immutables.copyOf(headers);
        consumes = Immutables.copyOf(consumes);
        produces = Immutables.copyOf(produces);
    }

    public static RequestDef of(RequestMapping source) {
        return RequestDef.builder()
                .paths(
                        Lists.collect(source.path())
                )
                .methods(
                        Lists.collect(source.method())
                )
                .headers(
                        Lists.collect(source.headers())
                )
                .consumes(
                        Lists.collect(source.consumes())
                )
                .produces(
                        Lists.collect(source.produces())
                )
                .build();
    }

    public RequestMethod method() {
        return methods.getFirst();
    }

    public String getPrimaryPath() {
        return paths.isEmpty() ? ""
                : paths.getFirst();
    }
}
