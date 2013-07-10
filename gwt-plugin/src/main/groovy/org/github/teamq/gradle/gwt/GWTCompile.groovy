package org.github.teamq.gradle.gwt

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction


/**
 * TODO open GitHub project and start discussion on gradle.org forum
 * 
 * the definitive gwt gradle plugin
 * 
 * @author dutkowskib
 * @see https://developers.google.com/web-toolkit/doc/latest/DevGuideCompilingAndDebugging#DevGuideCompilerOptions
 * 
 * {@link org.gradle.api.tasks.compile.AbstractCompile}
 * @see http://code.google.com/p/gwt-gradle-plugin/source/browse/trunk/src/main/groovy/com/pietschy/gradle/gwt/task/CompileGwt.groovy
 * @see http://www.itsolut.com/chrismusings/2009/08/11/gradle-gwt-logging-compile-errors/
 * @see http://www.prait.ch/wordpress/?p=347
 * @see https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/groovy/org/gradle/api/plugins/JavaPlugin.java
 */
class GWTCompile extends JavaExec {

	String group = 'Build'
	String description = 'GWT'

	/**
	 * The level of logging detail: ERROR, WARN, INFO, TRACE, DEBUG, SPAM, or ALL
	 */
	@Input @Optional
	String logLevel = 'INFO'

	void logLevel(String logLevel) {
		this.logLevel = logLevel
	}

	/**
	 * Script output style: OBF[USCATED], PRETTY, or DETAILED (defaults to OBF)
	 */
	@Input @Optional
	String style = 'OBF'

	void style(String style) {
		if(style == 'OBF' || style == 'PRETTY' || style == 'DETAILED') {
			this.style = style
		} else {
			throw new IllegalArgumentException("Argument style='${style}' not allowed, use OBF[USCATED], PRETTY, or DETAILED")
		}
	}

	/**
	 *  Debugging: causes the compiled output to check assert statements
	 */
	@Input @Optional
	boolean ea = false

	void ea(boolean ea) {
		this.ea = ea
	}

	/**
	 * EXPERIMENTAL: Disables some java.lang.Class methods (e.g. getName())
	 */
	@Input @Optional
	boolean disableClassMetadata = false

	void disableClassMetadata(boolean disableClassMetadata) {
		this.disableClassMetadata = disableClassMetadata
	}

	/**
	 * EXPERIMENTAL: Disables run-time checking of cast operations
	 */
	@Input @Optional
	boolean disableCastChecking = false

	void disableCastChecking(boolean disableCastChecking) {
		this.disableCastChecking = disableCastChecking
	}

	/**
	 * Validate all source code, but do not compile
	 */
	@Input @Optional
	boolean validateOnly = false

	void validateOnly(boolean validateOnly) {
		this.validateOnly = validateOnly
	}

	/**
	 * Speeds up compile with 25%
	 */
	@Input @Optional
	boolean draftCompile = false

	void draftCompile(boolean draftCompile) {
		this.draftCompile = draftCompile
	}

	/**
	 * Create a compile report that tells the Story of Your Compile 
	 */
	@Input @Optional
	boolean compileReport = false

	void compileReport(boolean compileReport) {
		this.compileReport = compileReport
	}

	/**
	 * The number of local workers to use when compiling permutations
	 */
	@Input @Optional
	int localWorkers = Runtime.getRuntime().availableProcessors();

	void localWorkers(int localWorkers) {
		this.localWorkers = localWorkers
	}

	@Input @Optional
	List<String> modules

	void modules(String ... modules) {
		if(modules == null || modules.length == 0) {
			throw new IllegalArgumentException("Argument 'modules' is null or empty")
		}
		if(this.modules == null) {
			this.modules = new ArrayList<String>(modules.length)
		}
		for(String m:modules) {
			this.modules.add(m)
		}
	}

	/**
	 * Only succeed if no input files have errors
	 */
	@Input @Optional
	boolean strict = true

	void strict(boolean strict) {
		this.strict = strict
	}

	/**
	 * Sets the optimization level used by the compiler.  0=none 9=maximum.
	 */
	@Input @Optional
	int optimize = -1

	void optimize(int optimize) {
		if(optimize >= 0 && optimize <= 9) {
			this.optimize = optimize
		} else {
			throw new IllegalArgumentException("Argument optimize='${optimize}' is out of range [0,9]")
		}
	}

	File buildDir
	
	@Optional
	File getBuildDir() {
		if (buildDir != null) {
			return buildDir;
		}
		
		return project.file("${project.buildDir}/${name}");
	}
	
	/**
	 * The compiler's working directory for internal use (must be writeable)
	 */
	File workDirectory

	@OutputDirectory @Optional
	File getWorkDirectory() {
		if (workDirectory != null) {
			return workDirectory;
		}
		
		return project.file("${getBuildDir()}/work")
	}
	
	/**
	 * The directory into which deployable output files will be written (defaults to 'war')
	 */
	File warDirectory

	@OutputDirectory @Optional
	File getWarDirectory() {
		if (warDirectory != null) {
			return warDirectory;
		}
		
		return project.file("${getBuildDir()}/war")
	}

	/**
	 *  The directory into which extra files, not intended for deployment, will be written
	 */
	File extraDir

	@OutputDirectory @Optional
	File getExtraDir() {
		if (extraDir != null) {
			return extraDir;
		}
		
		return project.file("${getBuildDir()}/extra")
	}

	/**
	 * Debugging: causes normally-transient generated types to be saved in the specified directory
	 */
	File generatedDir

	@OutputDirectory @Optional
	File getGeneratedDir() {
		if (generatedDir != null) {
			return generatedDir;
		}
		
		return project.file("${getBuildDir()}/generated")
	}

	/**
	 * The directory into which deployable but not servable output files will be written (defaults to 'WEB-INF/deploy' under the -war directory/jar, and may be the same as the -extra directory/jar)
	 */
	@OutputDirectory @Optional
	File deployDir = null

	GWTCompile() {
		outputs.upToDateSpec = new org.gradle.api.specs.AndSpec() //http://issues.gradle.org/browse/GRADLE-1483
	}

	@InputFiles
	Set<File> sourcesFiles = new HashSet<File>()

	/**
	 * @return the sourceFiles but completed with with some standard resources.
	 */
	Set<File> getSourcesFiles() {
		if(sourcesFiles.isEmpty()) {
			def main = project.sourceSets["main"]
			sourcesFiles.addAll(main.allSource.srcDirs)
			sourcesFiles.addAll(main.resources.srcDirs)
		}
		return sourcesFiles;
	}

	/**
	 * Compile the project with the GWT compiler. Uses "build/war" as staging directory.
	 *
	 * @see http://www.eveoh.nl/en/2012/01/using-google-web-toolkit-with-gradle/
	 */
	@TaskAction
	void exec () {
		classpath(getSourcesFiles())
		classpath(project.configurations[GWTPlugin.GWT_EXTENSION_NAME].files)

		setMain('com.google.gwt.dev.Compiler')
		
		maxHeapSize = '1024M'
		allJvmArgs.add("-XX:MaxPermSize=512M")

		buildArgs()
		
		super.exec()
	}

	/**
	 * Builds the arguments.
	 */
	private buildArgs() {
		args(modules)

		args('-war', getWarDirectory())
		args('-workDir', getWorkDirectory())
		args('-gen', getGeneratedDir())
		args('-extra', getExtraDir())
		
		args('-logLevel', logLevel)
		args('-style', style)

		if(draftCompile) {
			args('-draftCompile')
		}
		if(strict) {
			args('-strict')
		}
		if(compileReport) {
			args('-compileReport')
		}
		if(ea) {
			args('-ea')
		}
		if(disableClassMetadata) {
			args('-XdisableClassMetadata')
		}
		if(disableCastChecking) {
			args('-XdisableCastChecking')
		}
		if(validateOnly) {
			args('-validateOnly')
		}
		if(optimize >= 0 && optimize <= 9) {
			args('-optimize', optimize)
		}
		if(deployDir != null) {
			args('-deploy', deployDir)
		}
	}

}
