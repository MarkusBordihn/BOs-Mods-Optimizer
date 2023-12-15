# Welcome to the Mods Optimizer Wiki 📖

The Mods Optimizer is a Minecraft mod which automatically optimize the mods in the mod folder.
This is done by renaming / moving mods which are incompatible or not needed on the server side.

## How does it work ?

The Mods Optimizer is reading all `.jar` files in the `mods` folder and reading
the `mods.toml`, `fabric.mod.json` and `META-INF/mods.toml` files and the `MANIFEST.MF` file.
With this information the Mods Optimizer is able to classify the mods for the client and server
side.

In the case there are not enough information available the Mods Optimizer will use an internal list
of known mods to classify the mods.

If the mod is used on a server all client related mods will be automatically renamed
to `???.client`.
The mod is mostly helpful if it is used on the server and the client side.

Additionally, the Mods Optimizer is cleaning up duplicated files, by removing older versions based
on the version number and other information.

## Why is this mod needed ?

Regrettably, numerous mods face issues with accurately defining whether they belong to the client or
server side, and some even deviate from adhering to semantic versioning rules.
As a result, mod pack creators encounter challenges when attempting to construct a mod pack that
seamlessly functions on both the client and server side without individually verifying each mod.

Moreover, when updating mods on the server side, there is typically no built-in duplicate check.
Consequently, the `mods` folder may accumulate multiple versions of the same mod, necessitating
manual intervention to either manage these duplicates or resort to deleting the entire `mods` folder
before initiating the mod update.

## Can I classify specific mods ?

Yes, you can edit the config file `config/mods_optimizer/mods.toml` and add mod ids to classify them
for the client and server side.

Example:

```toml
[Mods]
ding = "client"
physicsmod = "client"
mousetweaks = "client"
client_side_mod_id = "client"
rubidium = "client"
server_side_mod_id = "server"
eatinganimation = "client"
```

## How can I update the internal list of known mods ?

Just delete the file `config/mods_optimizer/mods.toml` file and restart the game / server.
This will force the Mods Optimizer to read all mods again and create a
new `config/mods_optimizer/mods.toml` file.

## What's the difference to the Adaptive Performance Tweaks Mod Module ?

The Adaptive Performance Tweaks Mod Module is only using the file name to classify the mods.
This means it's not possible to classify mods which are not following the naming conventions or
using version numbers which could be not corrected.

The Mods Optimizer is reading the `META-INF/mods.toml`, `fabric.mod.json` and `MANIFEST.MF` file to
classify the mods.
This is more accurate and future proofed and will also work with mods which are not following the
naming conventions or using version numbers which could be not corrected.