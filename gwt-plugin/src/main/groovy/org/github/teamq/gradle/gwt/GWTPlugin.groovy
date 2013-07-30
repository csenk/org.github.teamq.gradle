/*
 * Copyright 2012 Christian Senk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.github.teamq.gradle.gwt

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.War
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.api.tasks.testing.Test


/**
 * @author senk.christian@googlemail.com
 */
class GWTPlugin implements Plugin<Project> {

    public static final String GWT_EXTENSION_NAME = "gwt";

    public static final String TEST_GWT_SOURCESET_NAME = "test-gwt";

    public static final String GWT_CONFIGURATION_NAME = "gwt";
    public static final String TEST_GWT_CONFIGURATION_NAME = "testGWT";

    public static final String COMPILE_GWT_TASK_NAME = "compileGWT";
    public static final String TEST_GWT_TASK_NAME = "testGWT";

    public static final String COMPILE_DEV_GWT_TASK_NAME = "compileDevGWT";
    public static final String DEV_WAR_TASK_NAME = "devWar";
    public static final String EXPLODE_DEV_WAR_TASK_NAME = "explodeDevWar";
    public static final String BUILD_DEV_WAR_TASK_NAME = "buildDevWar";

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(final Project project) {
        project.getPlugins().apply(JavaPlugin.class);

        configureConfigurations(project);

        def extension = project.extensions.create(GWT_EXTENSION_NAME, GWTPluginExtension.class, project)
        extension.version = '2.5.1'

        configureGWTCompile(project, extension)
        configureStandardGWTCompile(project)

        configureWarPlugin(project, extension)
        configureJavaPlugin(project)
        configureTestGWT(project)

        configureAbstractCompile(project)
    }

    /**
     * Configures {@link AbstractCompile} tasks to exclude all dependency artifacts from the both additional configurations.
     * 
     * @param project
     */
    private void configureAbstractCompile(final Project project) {
        project.tasks.withType(AbstractCompile.class) {
            it.doFirst { task ->
                FileCollection excludedFiles = project.files([
                    project.configurations[GWT_CONFIGURATION_NAME],
                    project.configurations[TEST_GWT_CONFIGURATION_NAME]]
                .collectMany({ configuration -> configuration.resolvedConfiguration.firstLevelModuleDependencies })
                .collectMany({ dependency -> dependency.moduleArtifacts })
                .collect({ artifact -> artifact.file }))

                task.classpath = task.classpath - excludedFiles
            }
        }
    }

    /**
     * @param project
     */
    private void configureTestGWT(final Project project) {
        SourceSet sourceSet = configureTestGWTSourceSet(project)
        if (sourceSet == null) {
            return
        }

        def task = project.tasks.add(TEST_GWT_TASK_NAME, Test.class)

        def workingDir = "${project.buildDir}${File.separator}${TEST_GWT_TASK_NAME}";
        task.dependsOn(project.tasks[COMPILE_GWT_TASK_NAME])

        task.workingDir = project.file(workingDir)
        task.systemProperties.put('gwt.args', "-out ${workingDir}")
        task.environment('gwt.persistentunitcachedir', workingDir)

        task.testSrcDirs = sourceSet.java.srcDirs.toList()
        task.testClassesDir = sourceSet.output.classesDir

        project.sourceSets.each {
            task.classpath += project.files(it.java.srcDirs) + project.files(it.resources.srcDirs) + it.output
        }
        task.classpath += sourceSet.runtimeClasspath + sourceSet.compileClasspath

        task.forkEvery = 1 //http://code.google.com/p/google-web-toolkit/issues/detail?id=5138

        task.doFirst {
            if (!task.workingDir.exists()) {
                task.workingDir.mkdirs()
            }
        }

        project.tasks["check"].dependsOn(task)
    }

    /**
     * @param project
     * @return
     */
    private SourceSet configureTestGWTSourceSet(final Project project) {
        File sourceDirectory = project.file("src${File.separator}test-gwt${File.separator}")
        if (!sourceDirectory.exists()) {
            return null;
        }

        return project.sourceSets.add(TEST_GWT_SOURCESET_NAME) {
            compileClasspath += project.configurations.getByName(JavaPlugin.TEST_COMPILE_CONFIGURATION_NAME)
            runtimeClasspath += project.configurations.getByName(JavaPlugin.TEST_RUNTIME_CONFIGURATION_NAME)
            runtimeClasspath += project.configurations.getByName(TEST_GWT_CONFIGURATION_NAME)

            SourceSet main = project.sourceSets.main
            compileClasspath += main.compileClasspath
            runtimeClasspath += main.runtimeClasspath
            compileClasspath += main.output
        }
    }

    /**
     * Configures the jar task to include java sources.
     * 
     * @param project
     */
    private void configureJavaPlugin(final Project project) {
        project.tasks[JavaPlugin.JAR_TASK_NAME].from(project.sourceSets.main.allJava)
    }

    /**
     * Configures the default dependency configurations 'gwt' and 'testGWT'. 
     * 
     * @param project the project
     */
    private void configureConfigurations(final Project project) {
        def configurations = project.getConfigurations();

        def compileConfiguration = configurations.getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME);
        def testCompileConfiguration = configurations.getByName(JavaPlugin.TEST_COMPILE_CONFIGURATION_NAME);

        def gwtConfiguration = configurations.create(GWT_CONFIGURATION_NAME);
        def testGWTConfiguration = configurations.create(TEST_GWT_CONFIGURATION_NAME);

        testGWTConfiguration.extendsFrom(gwtConfiguration);

        compileConfiguration.extendsFrom(gwtConfiguration);
        testCompileConfiguration.extendsFrom(testGWTConfiguration);
    }

    /**
     * Configures each {@link GWTCompile} task that would be ever created to use default values from {@link GWTPluginExtension}.
     * 
     * @param project the project
     * @param extension the extension registered for the project
     */
    private void configureGWTCompile(final Project project, final GWTPluginExtension extension) {
        project.tasks.withType(GWTCompile.class).whenTaskAdded { task ->
            task.dependsOn(project.tasks[JavaPlugin.CLASSES_TASK_NAME])

            task.doFirst {
                if (task.modules == null || task.modules.isEmpty()) {
                    task.modules = extension.modules
                }

                if (task.logLevel == null) task.logLevel = extension.logLevel
                if (task.style == null) task.style = extension.style
                if (task.ea == null) task.ea = extension.ea
                if (task.disableClassMetadata == null) task.disableClassMetadata = extension.disableClassMetadata
                if (task.disableCastChecking == null) task.disableCastChecking = extension.disableCastChecking
                if (task.validateOnly == null) task.validateOnly = extension.validateOnly
                if (task.draftCompile == null) task.draftCompile = extension.draftCompile
                if (task.compileReport == null) task.compileReport = extension.compileReport
                if (task.localWorkers == null) task.localWorkers = extension.localWorkers
                if (task.strict == null) task.strict = extension.strict
                if (task.optimize == null) task.optimize = extension.optimize
            }
        }
    }

    /**
     * Configures the project for an existent war plugin or for a war plugin that is added after this plugin.
     * 
     * @param project the project
     */
    private void configureWarPlugin(final Project project, final GWTPluginExtension extension) {
        def warPlugin = project.plugins.withType(WarPlugin.class).find();
        if (warPlugin != null) {
            configureWarPlugin(project, extension, warPlugin);
        }

        project.plugins.withType(WarPlugin.class).whenPluginAdded { plugin ->
            configureWarPlugin(project, extension, plugin)
        }
    }

    /**
     * Configures dependencies from this plugin to the war plugin.
     * This does also registers an independent task "buildDevWar" that builds an exploded war ready to
     * be used in development mode for example out of eclipse.
     * 
     * @param project the project
     * @param plugin the war plugin
     */
    private void configureWarPlugin(final Project project, final GWTPluginExtension extension, final WarPlugin plugin) {
        extension.updateDependencies(extension.version, extension.version);

        def compileGWTTask = project.tasks[COMPILE_GWT_TASK_NAME]
        def warTask = project.tasks[WarPlugin.WAR_TASK_NAME]

        warTask.dependsOn(compileGWTTask)
        warTask.from(compileGWTTask.getWarDirectory())

        def compileDevGWTTask = project.tasks.add(COMPILE_DEV_GWT_TASK_NAME, GWTCompile.class)
        compileDevGWTTask.style = "DETAILED"

        def devWarTask = project.tasks.add(DEV_WAR_TASK_NAME, War.class)
        devWarTask.from(compileDevGWTTask.getWarDirectory())
        devWarTask.dependsOn(compileDevGWTTask)
        devWarTask.appendix = 'dev'

        def explodeWarTask = project.task(EXPLODE_DEV_WAR_TASK_NAME) << {
            project.copy {
                from(project.zipTree(devWarTask.archivePath)) { exclude "**${File.separator}WEB-INF${File.separator}lib${File.separator}*" }
                into "${project.buildDir}${File.separator}devWar"
            }
            project.file(devWarTask.archivePath).delete()
        }
        explodeWarTask.dependsOn(devWarTask)

        def buildDevWarTask = project.task(BUILD_DEV_WAR_TASK_NAME)
        buildDevWarTask.dependsOn(explodeWarTask)
    }

    /**
     * Configures a standard "compileGWT" task that just is a standard gwt compile.
     * 
     * @param project
     */
    private void configureStandardGWTCompile(final Project project) {
        if (project.tasks.findByName(COMPILE_GWT_TASK_NAME) != null) {
            return;
        }

        def task = project.tasks.add(COMPILE_GWT_TASK_NAME, GWTCompile.class)
        project.tasks["build"].dependsOn(task)
    }

}
