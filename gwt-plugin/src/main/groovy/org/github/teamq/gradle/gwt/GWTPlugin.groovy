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

import java.awt.geom.Path2D.Double.CopyIterator;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.bundling.War;

/**
 * @author senk.christian@googlemail.com
 */
class GWTPlugin implements Plugin<Project> {

	public static final String GWT_EXTENSION_NAME = "gwt";
	
	public static final String GWT_CONFIGURATION_NAME = "gwt";
	public static final String TEST_GWT_CONFIGURATION_NAME = "testGWT";
	
	public static final String COMPILE_GWT_TASK_NAME = "compileGWT";
	
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
		
		def extension = project.extensions.create(GWT_EXTENSION_NAME, GWTPluginExtension.class, project);
		extension.version = '2.5.1'
		
		configureGWTCompile(project, extension)
		configureStandardGWTCompile(project)
		
		configureWarPlugin(project)
	}
	
	/**
	 * @param project
	 */
	private void configureConfigurations(final Project project) {
		def configurations = project.getConfigurations();

		def compileConfiguration = configurations.getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME);
		
		def gwtConfiguration = configurations.create(GWT_CONFIGURATION_NAME);
		def testGWTConfiguration = configurations.create(TEST_GWT_CONFIGURATION_NAME);
		
		gwtConfiguration.extendsFrom(compileConfiguration);
		testGWTConfiguration.extendsFrom(gwtConfiguration);
	}

	/**
	 * @param project
	 * @param extension
	 */
	private void configureGWTCompile(final Project project, final GWTPluginExtension extension) {
		project.tasks.withType(GWTCompile.class).whenTaskAdded { task ->
			task.doFirst {
				if (task.modules == null || task.modules.isEmpty()) {
					task.modules = extension.modules
				}
			}
		}
	}
	
	/**
	 * @param project
	 */
	private void configureWarPlugin(final Project project) {
		def warPlugin = project.plugins.withType(WarPlugin.class).find();
		if (warPlugin != null) {
			configureWarPlugin(project, warPlugin);
		}
		
		project.plugins.withType(WarPlugin.class).whenPluginAdded { plugin ->
			configureWarPlugin(project, plugin)
		}
	}
	
	/**
	 * @param project
	 * @param plugin
	 */
	private void configureWarPlugin(final Project project, final WarPlugin plugin) {
		def compileGWTTask = project.tasks[COMPILE_GWT_TASK_NAME]
		def warTask = project.tasks[WarPlugin.WAR_TASK_NAME]

		warTask.dependsOn(compileGWTTask)
		warTask.from(compileGWTTask.getWarDirectory())
		
		def compileDevGWTTask = project.tasks.add(COMPILE_DEV_GWT_TASK_NAME, GWTCompile.class)
		compileDevGWTTask.style = "DETAILED"
		
		def devWarTask = project.tasks.add(DEV_WAR_TASK_NAME, War.class)
		devWarTask.dependsOn(compileDevGWTTask)
		devWarTask.appendix = 'dev'
		
		def explodeWarTask = project.task(EXPLODE_DEV_WAR_TASK_NAME) << {
			project.copy {
				from project.zipTree(devWarTask.archivePath)
				into "${project.buildDir}/devWar"
				exclude "**/WEB-INF/lib/*"
			}
			project.file(devWarTask.archivePath).delete()
		}
		explodeWarTask.dependsOn(devWarTask)
		
		def buildDevWarTask = project.task(BUILD_DEV_WAR_TASK_NAME)
		buildDevWarTask.dependsOn(explodeWarTask)
	}
	
	/**
	 * @param project
	 */
	private void configureStandardGWTCompile(final Project project) {
		if (project.tasks.findByName(COMPILE_GWT_TASK_NAME) != null) {
			return;
		}
		
		def task = project.tasks.add(COMPILE_GWT_TASK_NAME, GWTCompile.class)
	}
	
}
