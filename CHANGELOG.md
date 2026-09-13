<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# bld Changelog

## [Unreleased]

- Replaced an internal IntelliJ Platform API.

## [0.8.0] - 2026-09-13

- Requires IntelliJ IDEA 2024.1 or later.
- Updated to intellij-platform-plugin 2.18.1 and Gradle 9.7.1.
- Replaced deprecated and internal IntelliJ Platform APIs.
- Added gutter icons to run the commands of the `bld` build file.
- Added automatic refresh when the `bld` build sources, the wrapper properties or the project SDK change.
- Improved loading of the `bld` commands and dependencies in the background, projects that don't use `bld` are skipped.
- Renamed the run configuration type that clashed with Ant, existing `bld` run configurations need to be recreated.
- Fixed failing before or after compile commands not stopping the compilation.
- Fixed a `bld` build that doesn't compile clearing the commands and dependencies.
- Fixed `bld` commands running at the same time compiling the build over each other.
- Fixed command actions staying registered after their project was closed.
- Fixed dependency names ending with a line break.
- Fixed Edit Main not working while the project is indexing.
- Fixed Edit Properties having the same icon as Edit Main in the new UI.

## [0.7.2] - 2024-11-14

- Updated for latest IntelliJ IDEA.

## [0.7.1] - 2024-10-14

- Updated to intellij-platform-plugin 2.0.2.

## [0.7.0] - 2024-08-27

- Fix for execution hanging when plugin is not initialized.

## [0.6.4] - 2024-08-19

- Updated to intellij-platform-plugin 2.0.1.

## [0.6.3] - 2024-07-30

- Updated to intellij-platform-plugin 2.0.0.
- Plugin description tweaks.

## [0.6.2] - 2024-07-30

- Added scroll to top and scroll to bottom icons in the `bld` console.
- Fixed project browser tooltips wrong displaying on folders and dependencies.

## [0.6.1] - 2024-07-30

- Fixed run configuration `bld` commands not executing asynchronously.

## [0.6.0] - 2024-07-29

- Added assigning keyboard shortcuts to `bld` commands.
- Fixed command-line not displayed when executing before or after compile commands.
- Improved `bld` icon used for run configurations.

## [0.5.0] - 2024-07-28

- Initial release for `bld` 2.0.1.

[Unreleased]: https://github.com/rife2/bld-idea/compare/v0.8.0...HEAD
[0.8.0]: https://github.com/rife2/bld-idea/compare/v0.7.2...v0.8.0
[0.7.2]: https://github.com/rife2/bld-idea/compare/v0.7.1...v0.7.2
[0.7.1]: https://github.com/rife2/bld-idea/compare/v0.7.0...v0.7.1
[0.7.0]: https://github.com/rife2/bld-idea/compare/v0.6.4...v0.7.0
[0.6.4]: https://github.com/rife2/bld-idea/compare/v0.6.3...v0.6.4
[0.6.3]: https://github.com/rife2/bld-idea/compare/v0.6.2...v0.6.3
[0.6.2]: https://github.com/rife2/bld-idea/compare/v0.6.1...v0.6.2
[0.6.1]: https://github.com/rife2/bld-idea/compare/v0.6.0...v0.6.1
[0.6.0]: https://github.com/rife2/bld-idea/compare/v0.5.0...v0.6.0
[0.5.0]: https://github.com/rife2/bld-idea/commits/v0.5.0
