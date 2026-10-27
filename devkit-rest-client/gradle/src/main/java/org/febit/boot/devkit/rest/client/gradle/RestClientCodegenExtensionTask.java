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
package org.febit.boot.devkit.rest.client.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.classpath.DefaultClassPath;

import org.febit.boot.devkit.rest.client.ClientCodegen;
import org.febit.boot.devkit.rest.client.MetaResolver;
import org.febit.boot.devkit.rest.client.util.ClassNamingImpl;
import org.febit.boot.devkit.rest.client.util.ClassNamings;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;

import java.net.URLClassLoader;
import javax.inject.Inject;

@Slf4j
@NullMarked
@CacheableTask
public abstract class RestClientCodegenExtensionTask extends DefaultTask {

    /**
     * Classes to scan, typically the output of the source project.
     */
    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getSourceClasspath();

    @Input
    public abstract SetProperty<String> getScanPackages();

    @Input
    public abstract SetProperty<String> getExcludedClasses();

    @Input
    @Optional
    public abstract Property<String> getBasePackage();

    @Input
    public abstract ListProperty<String> getNamingSignatures();

    @Internal
    public abstract Property<ClassNamingImpl> getGlobalNaming();

    @Internal
    public abstract Property<ClassNamingImpl> getClientNaming();

    @Internal
    public abstract Property<ClassNamingImpl> getPojoNaming();

    @OutputDirectory
    public abstract DirectoryProperty getTargetDir();

    @Inject
    public RestClientCodegenExtensionTask() {
        setGroup(Constants.GROUP_NAME);
        setDescription("Generate api definitions as REST clients.");
    }

    @TaskAction
    public void run() {
        var classloader = resolveClassLoader();

        var metaResolver = MetaResolver.create(classloader);

        var beans = metaResolver.scanBeans(getScanPackages().get());
        var clients = metaResolver.resolveClients(beans.values());

        log.info("Found [{}] clients", clients.size());

        var globalNaming = getGlobalNaming().get();

        var codegen = ClientCodegen.builder()
                .beans(beans)
                .clients(clients)
                .clientBasePackage(getBasePackage().getOrNull())
                .clientNaming(ClassNamings.chain(
                        globalNaming,
                        getClientNaming().get()
                ))
                .pojoNaming(ClassNamings.chain(
                        globalNaming,
                        getPojoNaming().get()
                ))
                .excludedClasses(getExcludedClasses().get())
                .targetDir(getTargetDir().getAsFile().get())
                .build();

        codegen.emit();
    }

    private ClassLoader resolveClassLoader() {
        var urls = DefaultClassPath.of(getSourceClasspath().getFiles())
                .getAsURLArray();

        // NOTE: Using current classloader as parent.
        return new URLClassLoader(urls, getClass().getClassLoader());
    }
}
