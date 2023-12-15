# Define the correct environment for a Mod

It's important to define the correct environment for a Mod to make sure a client-side mod is not
crashing a server or in rare cases that a server-side mod is not crashing a client.

## How to define the correct environment for a Mod?

Depending on the mod loader you are using, you have to define the correct environment for your mod.

### Forge

For Forge there are two options to define the correct environment for a mod.

#### Dependencies inside the `mods.toml` file

The 'mods.toml' file is located inside the `META-INF` folder of the mod jar file.
Its includes the dependencies and the environment for the mod.

There is normally a dependency section for `forge` and `minecraft` which allows to define the
correct side for the mod.

Example:

```toml
[[mods]]
modId = "example_mod"

[[dependencies.example_mod]]
modId = "forge"
mandatory = true
versionRange = "[36.1.0,)"
ordering = "NONE"
side = "CLIENT"
[[dependencies.example_mod]]
modId = "minecraft"
mandatory = true
versionRange = "[1.16.5,)"
ordering = "NONE"
side = "CLIENT"
```

The `side` parameter can be `CLIENT`, `SERVER` or `BOTH`:

- `CLIENT` means that the mod is physically loaded on the client side.
- `SERVER` means that the mod is physically loaded on the server side.
- `BOTH` means that the mod physically loaded on the client and server side.

See: https://docs.minecraftforge.net/en/1.20.x/gettingstarted/modfiles/

#### Display Test

The `displayTest` annotation is used to show a warning message if the mod is loaded on the wrong
side.
This is mostly used on servers to show you a warning message before you are connecting to the
server.

You can define the `displayTest` inside the `mods.toml` file like:

```toml
[[mods]]
modId = "example_mod"
displayName = "Example Mod"
displayURL = "https://www.example.com"
displayAuthors = "Markus Bordihn"
displayDescription = "This is an example mod."
displayTest = "MATCH_VERSION"
```

The `displayTest` parameter can be `MATCH_VERSION`, `IGNORE_SERVER_VERSION`, `IGNORE_ALL_VERSION`
or `NONE`:

- `MATCH_VERSION`
  Use this option if your mod involves both server and client elements.
  A red X will appear if the versions on the client and server differ.
  This is the default behavior for mods with server and client components.
- `IGNORE_SERVER_VERSION`
  Ideal for server-only mods.
  Your mod won't trigger a red X if it's present on the server but not on the client.
  Recommended for mods that exclusively function on the server side.
- `IGNORE_ALL_VERSION`
  A special case for mods with no server component like pure client-side mods.
  Won't cause a red X whether present on the client or the server.
  Use this option only if your mod lacks a server component.
- `NONE`
  No display test is set by default.
  You must define your own display test using IExtensionPoint.DisplayTest inside your mod's code.
  This option allows you to create a custom scheme based on your mod's requirements.

See: https://docs.minecraftforge.net/en/1.20.x/concepts/sides/#writing-one-sided-mods

### Fabric

For Fabric there is only one option to define the correct environment for a mod inside
the `fabric.mod.json` file.

Example:

```json
{
  "schemaVersion": 1,
  "id": "example_mod",
  "version": "1.0.0",
  "name": "Example Mod",
  "description": "This is an example mod.",
  "authors": [
    "Markus Bordihn"
  ],
  "contact": {
    "homepage": "https://www.example.com"
  },
  "license": "MIT",
  "icon": "assets/example_mod/icon.png",
  "environment": "client"
}
```

The `environment` parameter can be `client`, `server` or `*`:

- `*` Runs everywhere. Default for client and server mods.
- `client` Runs on the client side for client side mods.
- `server` Runs on the server side for server side mods.

See: https://fabricmc.net/wiki/documentation:fabric_mod_json_spec
