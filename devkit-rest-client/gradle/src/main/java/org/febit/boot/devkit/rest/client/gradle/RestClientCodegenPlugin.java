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

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;

import org.febit.devkit.gradle.util.FolderUtils;
import org.febit.devkit.gradle.util.GradleUtils;

import java.util.List;

public class RestClientCodegenPlugin implements Plugin<Project> {

    @Override
    public void apply(Project proj) {
        proj.getPlugins().apply(JavaBasePlugin.class);

        proj.getExtensions()
                .create(Constants.EXT_NAME, RestClientCodegenExtension.class, proj);

        proj.getTasks()
                .register(Constants.TASK_GEN_CLIENT, RestClientCodegenExtensionTask.class);

        proj.afterEvaluate(this::afterEvaluate);
    }

    private void afterEvaluate(Project proj) {
        var extension = proj.getExtensions()
                .getByType(RestClientCodegenExtension.class);

        var targetDir = extension.getTargetSourceDir();
        FolderUtils.mkdirs(targetDir);
        GradleUtils.mainSourceSet(proj).getJava().srcDir(targetDir);

        var sourceProj = extension.getSourceProject();
        if (sourceProj == null) {
            throw new GradleException(Constants.EXT_NAME + ".sourceProject is not set.");
        }

        var naming = extension.getNaming();
        var tasks = proj.getTasks();

        // Depend on lombok plugin
        tasks.named(Constants.TASK_GEN_LOMBOK_CONFIG, task ->
                task.dependsOn(Constants.TASK_GEN_CLIENT)
        );

        tasks.named(Constants.TASK_GEN_CLIENT, RestClientCodegenExtensionTask.class, task -> {
            task.dependsOn(sourceProj.getTasks()
                    .getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME));

            // Inputs & outputs, so that the task can be UP-TO-DATE or loaded from cache.
            task.getSourceClasspath().from(
                    GradleUtils.mainSourceSet(sourceProj).getOutput(),
                    sourceProj.getConfigurations()
                            .getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME)
            );
            task.getTargetDir().set(targetDir);
            task.getScanPackages().set(extension.getScanPackages());
            task.getExcludedClasses().set(extension.getExcludedClasses());
            task.getBasePackage().set(extension.getClient().getBasePackage());
            task.getNamingSignatures().set(List.of(
                    "global:" + naming.getGlobal().signature(),
                    "client:" + naming.getClient().signature(),
                    "pojo:" + naming.getPojo().signature()
            ));
            task.getGlobalNaming().set(naming.getGlobal());
            task.getClientNaming().set(naming.getClient());
            task.getPojoNaming().set(naming.getPojo());
        });
    }

}
