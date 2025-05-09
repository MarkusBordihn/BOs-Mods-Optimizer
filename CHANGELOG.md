# Changelog for Mods Optimizer

## Note

This change log includes the summarized changes.
For the full changelog, please go to the [GitHub History][history] instead.

### v4.0.0

- Fixed #37 by adding `AttributeFix`, `Emojiful` and `BetterBurning` to the both mod lists.
- Fixed #36 by adding `zume` to client side mod list.
- Added remote mods database sync for automatic updates, as long the `mods-database.json` file is
  not modified.

### v3.0.0

- Fixed #32 by moving `Better Clouds Reforged` to the default mod list.
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
- Added mods reader for `farbric.mod.json` files.
- Added automatic sem-version correction for mod versions.
- Added mods.toml configuration file to classify mods for client and server.

### v1.0.0

- Initial release of Mods Optimizer (will replace Adaptive Performance Tweaks Mod Module).

[history]: https://github.com/MarkusBordihn/BOs-Mods-Optimizer/commits/main

[things_pack]: https://www.curseforge.com/minecraft/modpacks/things-pack
