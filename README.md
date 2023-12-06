[![Mods Optimizer Downloads](http://cf.way2muchnoise.eu/full_947247_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/mods-optimizer)
[![Mods Optimizer Versions](http://cf.way2muchnoise.eu/versions/Minecraft_947247_all.svg)](https://www.curseforge.com/minecraft/mc-mods/mods-optimizer)

![Mods Optimizer: Mods][logo]

Mods Optimizer is a collection of Minecraft Forge server-side Mod which automatically
adjust specific settings on the server to allow a more balanced TPS/FPS.
The goal of this mod is to allow a smoother experience on a server with several (=> 180) Mods.

## Please read before using ⚠️

This mod is renaming / moving your mod files, for this reason it is important that you have regular
backups in place.
I will do my best to implement corresponding safety features to avoid possible issues, but I'm not
able to cover all use cases.

## 👾Automatic Server Bundle (one mod pack for client/server)

This mod automatically disable mods which are incompatible or not needed on the server side.
There is no longer the need to have a separated "server" and "client" mod pack only for the mods.

## 👾 Automatic Mod Cleanup

This mod automatically clean up duplicated files, by removing older versions.
This is helpful to avoid duplication issues and manual deleting of older versions.

## ⏱️ Total Start Time Logging

Measure and logs the total start time of the client and server inside the log.
This makes it ideal to optimize the loading time of mods packs without using a manual stop clock.

## How to use the mod ?

Just add the mod to your mod pack and during the start on a Minecraft Client or a Minecraft Server
it will automatically optimize the mods in the mod folder.

If the mod is used on a server all client related mods will be automatically renamed to "
???.client".
The mod is mostly helpful if it is used on the server and the client side.

## Example Mod Pack

If you want to see this mod in action, please take a look at:
https://www.curseforge.com/minecraft/modpacks/bos-adventure-world

[logo]: src/main/resources/logo.png
