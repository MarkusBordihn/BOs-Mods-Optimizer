# Changelog for Mods Optimizer

## Note

This change log includes the summarized changes.
For the full changelog, please go to the [GitHub History][history] instead.

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
