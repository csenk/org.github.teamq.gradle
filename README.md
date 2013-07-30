This gradle plugin focuses on providing an easy gradle integration to build and test GWT applications and libraries.
Although this plugin works nicely with the [War Plugin](http://www.gradle.org/docs/current/userguide/war_plugin.html) it is
not necessary to apply it as well. As GWT relies on Java the [Java Plugin](http://www.gradle.org/docs/current/userguide/java_plugin.html)
is applied automatically, but you are free to apply it yourself as well.

## Usage

By now there is no built version of this plugin. You have to clone this repository and build it yourself with gradle.
But as you searched a gwt plugin for gradle i can safely assume that you already familiar with gradle.

To build this plugin, clone this repository and call:

	cd gwt-plugin
	gradle install
	
After you installed this plugin into your local maven/gradle caches this way, you are able to reference it right in the top
section of your build script:

	buildscript {
		repositories {
			mavenLocal()
		}
		
		dependencies {
			classpath group: 'org.github.teamq.gradle', name: 'gwt-plugin', version: '0.0.1-SNAPSHOT'
		}
	}

The actual usage is quite simple and like every other gradle plugin.

	apply plugin: 'gwt'

## Tasks

The GWT plugin adds a bunch of tasks

| Task name     | Depends on           | Type       | Description                                               |
| ------------- | -------------------- | ---------- | --------------------------------------------------------- |
| compileGWT    | *classes*            | GWTCompile | Compiles GWT modules. Runs with the normal *build* task.  |

If the War plugin is applied as well, some further tasks are added. *compileGWT* and it´s output also contributes to the *war* task.

| Task name     | Depends on           | Type       | Description                                               |
| ------------- | -------------------- | ---------- | --------------------------------------------------------- |
| compileDevGWT | *classes*            | GWTCompile | Compiles GWT modules for an exploded development war      |
| devWar        | *compileDevGWT*      | War        | Creates the same output as *war* but with *dev* as suffix |
| explodeDevWar | *devWar*             | Task       | Takes the previously assembled war, unzips and deletes it |
| buildDevWar   | *explodeDevWar*      | Task       | Just a generic task to be more convenient                 |

If the source folder *src/test-gwt* is present, the GWT plugin adds a test task as wells as a source-set

| Task name     | Depends on           | Type       | Description                                                  |
| ------------- | -------------------- | ---------- | ------------------------------------------------------------ |
| testGWT       | *compileGWT*         | Test       | Runs all test cases defined in the source-folder *test-gwt*. |

## Project layout

The GWT plugin expects a new optional source folder *src/test-gwt*. If present the GWT plugin is going to configure a source-set named *test-gwt*
and a task called *testGWT*.

## Dependency management

Two new dependency configurations are introduced by the GWT plugin as shown below.

| Name          | Contributes to       | Used by tasks | Meaning                                       |
| ------------- | -------------------- | ------------- | --------------------------------------------- |
| gwt           | compile, testGWT     | GWTCompile    | Dependencies especially for the GWT compiler. |
| testGWT       | testCompile          | testGWT       | Same as *gwt* but only for tests              |

The plugin will take care of the proper dependencies. Even if the war plugin is applied as well, the plugin takes care of
adding *gwt-servlet* as a dependency. As *gwt-dev* is added to the *gwt* configuration by default it is sometimes
necessary to add *gwt-dev* to compile as well. This decision should be done careful as *gwt-dev* contains way more
classes than just GWT related stuff, it sometimes can appear that you are expecting that the classloader loads a classes
while *gwt-dev* shows up earlier in the classpath carrying an older version of this class. This normally leads to
NoSuchMethod exceptions or similar.

But anyway, here is how you add *gwt-dev* to compile as well.

	compile gwt.devSDK()

You only have to make sure, that you call this method **after** you set the right GWT version. This is because *devSDK()*
generates a static dependency notation.

## Configuration

By default the GWT plugin will use version '2.5.1' of GWT as it was the latest version during the development of the plugin.
But you are free to change this easily:

	gwt.version = '2.4.0'

The GWT plugin adds the chance of configuring some GWT and compile related properties globally for the project.
Each GWTCompile task you use or define by yourself will initially inherit these properties. But you are everytime
free to configure each GWTCompile task independently.