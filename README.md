# bld IntelliJ IDEA Support

[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Version](https://img.shields.io/jetbrains/plugin/v/24980-bld.svg)](https://plugins.jetbrains.com/plugin/24980-bld)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/24980-bld.svg)](https://plugins.jetbrains.com/plugin/24980-bld)
![Build](https://github.com/rife2/bld-idea/workflows/Build/badge.svg)

<img src="https://rife2.com/images/bld-idea.png" style="width: 100%">

<!-- Plugin description -->
Support for the `bld` pure Java build tool: https://rife2.com/bld

* detect `bld` projects and find their main Java class
* quick access to open and edit the main Java class and wrapper properties of `bld` projects
* list all the commands in `bld` projects in a side panel
* execute one or multiple commands in the order they were selected
* reload the commands in the `bld` project
* terminate currently running `bld` commands
* `bld` console panel for command output with source code hyperlinking
* display the `bld` dependency tree
* toggle to run `bld` in offline or online mode
* auto-save all open files before executing a `bld` command
* convenient `bld` one-click cache invalidation
* set `bld` commands to run before or after IDEA compilation
* create custom `bld` command run configuration with options, JVM arguments, and before launch tasks
* assign keyboard shortcuts to `bld` commands

Projects need to use `bld` `v2.0` or later.

<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "bld"</kbd> >
  <kbd>Install</kbd>
  
- Manually:

  Download the [latest release](https://github.com/rife2/bld-idea/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
