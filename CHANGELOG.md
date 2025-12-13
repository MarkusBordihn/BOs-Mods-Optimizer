# Changelog for Mods Optimizer

## Note

This change log includes the summarized changes.
For the full changelog, please go to the [GitHub History][history] instead.

### v4.3.0

- Fixed #55, #54 by `adding mods-database-override.json` which will never be overwritten for
  consistent manual
  changes by the user.
- Fixed crashes by moving `midnightlib` to client and server mod list.
- Fixed issue with automatic updates and wrongly calculated hash values.
- Added `mods-database-override.json` support and improved automatic updates for
  `mods-database.json`.
- Added memory resources clean up after run.
- Improved unit tests and added additional test files for mods-database.json and
  mods-database-override.json.
- Increased online update check interval to 48 hours.

### v4.2.0

- Fixed #45 by adding `Create Cyber Googles`, `Crearte Schematics` and `JER - Just Enough Resources`
  to client
  side mod list.
- Added unit tests for timestamp parsing functionality.
- Added preprocessing for TOML files to handle invalid keys and improve logging.
- Improved support for NeoForge mod type detection.
- Improved timestamp parsing to support multiple formats and handle invalid timestamps gracefully.

### v4.1.0

- Fixed #41 by adding support for "alternative" mods.toml entries.
- Fixed #40 by adding `Smithing Template Viewer` to the client side mod list.
- Added mod file parsing support for several "alternative" mods.toml entries.
- Added support for `neoforge.mods.toml` files and better global language support.
- Added additional test files and seperated test for ModData(raw) and ModsDatabase Tests.

### v4.0.0

- Fixed #37 by adding `AttributeFix`, `Emojiful` and `BetterBurning` to the both mod lists.
- Fixed #36 by adding `zume` to client side mod list.
- Added remote mods database sync for automatic updates, as long the `mods-database.json` file is
  not modified.

### v3.0.0

- Fixed #32 by moving `Distant Horizons` to the default mod list.
- Fixed #31 by adding `Better Clouds Reforged` to the client side mod list.
- Fixed #30 by adding `Create: Fuel & Water Information` to the client side mod list.
- Fixed #29 by adding `Elytra Utilities` to the client side mod list.
- Fixed #28 by adding `Fog` to the client side mod list.
- Fixed #27 by adding `Inventory Tweaks - Refoxed` to default mod list.
- Fixed #26 by adding `Better Grassify` to the client side mod list.
- Fixed #25 by adding `Better Clouds` to the client side mod list.
- Fixed #23 by adding `Sodium` related mods to the client side mod list.
- Fixed duplicated key crash within the `config.toml` file.
- Added pre-check for `config.toml` file to avoid crashes on invalid list entries.
- Improved code quality and fixed smaller issues.

### v2.1.0

- Fixed #22 by refactored code for 1.20.6 and higher.
- Fixed #21, #20, #18 by updating internal client side database.
- Added additional test cases.

### v2.0.0

- Added JSON parse with additional checks, to avoid crashes on invalid .json files. #17
- Added TOML parse with additional checks, to avoid crashes on invalid .toml files.
- Added additional warnings for invalid mod files.
- Improved code quality and added additional tests.

### v.1.8.0

- Updated known client side mods. Thanks to `@CorneliusCornbread`.
- Small code optimizations and improvements.

### v1.7.0

- Added mods reader for `quilt.mod.json` files. #13
- Added detection for service mods like `Sinytra Connector`. #13
- Added additional tests for mod detection, especially for mixed mods and cross-loaded mods.
- Improved duplication detection by adding additional checks for mod versions and mod ids.
- General Code optimizations and improvements.

### v1.6.0

- Updated known client side and server side mods. Thanks to `@adamk33n3r`, `@ChangeOtaku`
  and `@Jadan1213`.

### v1.5.0

- Added support for [Things pack][things_pack].
- Improved general Forge, Fabric and NeoForge mod detection.
- Improved data pack detection.

### v1.4.0

- Added support for mixed mods and data pack mods.
- Added additional documentation for the config.toml configuration file.
- Reduced the numbers of log messages for non-debug mode.
- Improved error messages.

### v1.3.0

- Fixed Jar in Jar conflict with mods which are using the same package names.

### v1.2.0

- Added support for library mods and language provider mods.
- Added fallback mod id detection for mods without `mods.toml` or `fabric.mod.json` files.
- Improved Mod Type detection.
- Smaller code optimizations and improvements.

### v1.1.0

- Added mods reader for `mods.toml` files.
- Added mods reader for `fabric.mod.json` files.
- Added automatic sem-version correction for mod versions.
- Added mods.toml configuration file to classify mods for client and server.

### v1.0.0

- Initial release of Mods Optimizer (will replace Adaptive Performance Tweaks Mod Module).

[history]: https://github.com/MarkusBordihn/BOs-Mods-Optimizer/commits/1.21.5

[things_pack]: https://www.curseforge.com/minecraft/modpacks/things-pack
