This gradle plugin focuses on providing an easy gradle integration to build and test GWT applications and libraries.
Although this plugin works nicely with the [War Plugin](http://www.gradle.org/docs/current/userguide/war_plugin.html) it is
not necessary to apply it as well. As GWT relies on Java the [Java Plugin](http://www.gradle.org/docs/current/userguide/java_plugin.html)
is applied automatically, but you are free to apply it yourself as well.

## Usage

By now there is no built version of this plugin. You have to clone this repository and build it yourself with gradle.
But as you searched a gwt plugin for gradle i can safely assume that you already familiar with gradle.

To build this plugin, clone this repository and call:

	gradle :gwt-plugin:install
	
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
	
## Source sets

## Tasks

The GWT plugin adds a bunch of tasks

| Task name     | Depends on           | Type       | Description                |
| ------------- | -------------------- | ---------- | -------------------------- |
| compileGWT    | *classes*            | GWTCompile | Compiles GWT modules       |

If the War plugin is applied as well, some further tasks are added. *compileGWT* and it's output also contributes to the *war* task.

| Task name     | Depends on           | Type       | Description                                               |
| ------------- | -------------------- | ---------- | --------------------------------------------------------- |
| compileDevGWT | *classes*            | GWTCompile | Compiles GWT modules for an exploded development war      |
| devWar        | *compileDevGWT*      | War        | Creates the same output as *war* but with *dev* as suffix |
| explodeDevWar | *devWar*             | Task       | Takes the previously assembled war, unzips and deletes it |
| buildDevWar   | *explodeDevWar*      | Task       | Just a generic task to be more convenient                 |

## Project layout

## Dependency management

## Extension properties