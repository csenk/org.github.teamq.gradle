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

import org.gradle.api.Project
import org.gradle.api.plugins.WarPlugin

/**
 * @author senk.christian@googlemail.com
 */
class GWTPluginExtension {

	private final Project project;
	
	GWTPluginExtension(final Project project) {
		this.project = project;
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
	
	List<String> modules

	List<String> getModules() {
		if (modules != null && !modules.isEmpty()) {
			return modules;
		}
		
		def mainSourceSet = project.sourceSets.main
		return mainSourceSet.java.plus(mainSourceSet.resources)
			.filter({ it.name.endsWith("gwt.xml") })
			.collect({
				def moduleName = it.path
				mainSourceSet.java.srcDirs.each { moduleName = moduleName.replace(it.path, '') }
				mainSourceSet.resources.srcDirs.each { moduleName = moduleName.replace(it.path, '') }
				return moduleName.substring(1).replaceAll('(\\\\|/)', '\\.').replace(".gwt.xml", '')
			})
	}
	
	void setModules(String... modules) {
		if (modules == null || modules.length == 0) {
			throw new IllegalArgumentException("Argument 'modules' is null or empty")
		}

		this.modules = []
		this.modules = modules
	}
	
	/**
	 * The level of logging detail: ERROR, WARN, INFO, TRACE, DEBUG, SPAM, or ALL
	 */
	String logLevel = 'INFO'
	
	void setLogLevel(String logLevel) {
		if(logLevel == 'ERROR' || logLevel == 'WARN' || logLevel == 'INFO' || logLevel == 'TRACE' || logLevel == 'DEBUG' || logLevel == 'SPAM' || logLevel == 'ALL') {
			this.logLevel = logLevel
		} else {
			throw new IllegalArgumentException("Argument logLevel='${logLevel}' not allowed, use ERROR, WARN, INFO, TRACE, DEBUG, SPAM, or ALL")
		}
	}

	/**
	 * Script output style: OBF[USCATED], PRETTY, or DETAILED (defaults to OBF)
	 */
	String style = 'OBF'

	void setStyle(String style) {
		if(style == 'OBF' || style == 'PRETTY' || style == 'DETAILED') {
			this.style = style
		} else {
			throw new IllegalArgumentException("Argument style='${style}' not allowed, use OBF[USCATED], PRETTY, or DETAILED")
		}
	}
	
	/**
	 *  Debugging: causes the compiled output to check assert statements
	 */
	boolean ea = false
	
	/**
	 * EXPERIMENTAL: Disables some java.lang.Class methods (e.g. getName())
	 */
	boolean disableClassMetadata = false
	
	/**
	 * EXPERIMENTAL: Disables run-time checking of cast operations
	 */
	boolean disableCastChecking = false
	
	/**
	 * Validate all source code, but do not compile
	 */
	boolean validateOnly = false
	
	/**
	 * Speeds up compile with 25%
	 */
	boolean draftCompile = false
	
	/**
	 * Create a compile report that tells the Story of Your Compile
	 */
	boolean compileReport = false
	
	/**
	 * The number of local workers to use when compiling permutations
	 */
	int localWorkers = Runtime.getRuntime().availableProcessors();
	
	/**
	 * Only succeed if no input files have errors
	 */
	boolean strict = true
	
	/**
	 * Sets the optimization level used by the compiler.  0=none 9=maximum.
	 */
	int optimize = 0

	void setOptimize(int optimize) {
		if(optimize >= 0 && optimize <= 9) {
			this.optimize = optimize
		} else {
			throw new IllegalArgumentException("Argument optimize='${optimize}' is out of range [0,9]")
		}
	}
	
}
