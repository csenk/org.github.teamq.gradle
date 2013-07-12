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

import java.util.List;

import org.gradle.api.Project
import org.gradle.api.plugins.WarPlugin;

/**
 * @author senk.christian@googlemail.com
 */
class GWTPluginExtension {

	private final Project project;
	
	GWTPluginExtension(final Project project) {
		this.project = project;
	}
	
	List<String> modules

	void setModules(String... modules) {
		if (modules == null || modules.length == 0) {
			throw new IllegalArgumentException("Argument 'modules' is null or empty")
		}

		if (this.modules == null) {
			this.modules = new ArrayList<String>(modules.length)
		}

		for (String m : modules) {
			this.modules.add(m)
		}
	}
	
	String version
	
	/**
	 * @param version the version to set.
	 */
	void setVersion(String version) {
		updateDependencies(this.version, version)
		
		this.version = version;
	}

	/**
	 * @param oldVersion
	 * @param newVersion
	 * @return
	 */
	protected updateDependencies(final String oldVersion, final String newVersion) {
		removeAndAddDependency('compile', 'com.google.gwt', 'gwt-user', oldVersion, newVersion);
		removeAndAddDependency('gwt', 'com.google.gwt', 'gwt-dev', oldVersion, newVersion);

		if (project.plugins.findPlugin(WarPlugin.class) != null) {
			removeAndAddDependency('compile', 'com.google.gwt', 'gwt-servlet', oldVersion, newVersion);
		}
	}

	
	
	/**
	 * @return {@link Dependency} notation with the configured {@link #version} to use in {@link DependencyHandler}.
	 */
	String userSDK() {
		return "com.google.gwt:gwt-user:${version}"
	}
	
	/**
	 * @return {@link Dependency} notation with the configured {@link #version} to use in {@link DependencyHandler}.
	 */
	String devSDK() {
		return "com.google.gwt:gwt-dev:${version}"
	}
	
	/**
	 * Removes a dependency with an older version prior to adding it with the new version.
	 * 
	 * @param configuration the configuration name.
	 * @param group the dependency group.
	 * @param name the dependency name.
	 * @param oldVersion the old dependency version.
	 * @param updateVersion the old dependency version.
	 */
	private removeAndAddDependency(String configuration, String group, String name, final String oldVersion, final String newVersion) {
		def dependencyIterator = project.configurations[configuration].dependencies.iterator()
		while (dependencyIterator.hasNext()) {
			def dependency = dependencyIterator.next();

			if (dependency.group == group && dependency.name == name && dependency.version == oldVersion) {
				dependencyIterator.remove();
				break;
			}
		}

		project.dependencies.add(configuration, "${group}:${name}:${newVersion}")
	}
	
}
