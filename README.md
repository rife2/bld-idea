# bld IntelliJ IDEA Support

[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
![Build](https://github.com/rife2/bld-idea/workflows/Build/badge.svg)

<!-- Plugin description -->
IntelliJ IDEA support for the `bld` pure Java build tool: https://rife2.com/bld

The `bld` IDEA plugin supports:
* detecting `bld` projects and finding their main Java class
* quick access to open and edit the main Java class of `bld` projects
* quick access to open and edit the wrapper properties of `bld` projects
* listing all the commands in `bld` projects in a side panel
* executing one or multiple commands in the order they were selected
* reloading the commands in the `bld` project
* terminating currently running `bld` commands
* `bld` console output panel for detailed information
* auto-save all open files before executing a `bld` command

<!-- Plugin description end -->

## Installation (not published yet)

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
