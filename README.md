[![Mods Optimizer Downloads](http://cf.way2muchnoise.eu/full_947247_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/mods-optimizer)
[![Mods Optimizer Versions](http://cf.way2muchnoise.eu/versions/Minecraft_947247_all.svg)](https://www.curseforge.com/minecraft/mc-mods/mods-optimizer)

![Mods Optimizer][logo]

# ⚠️ Please Read Before Using

**Mods Optimizer** will **rename or move files** inside your `mods` folder.  
➡️ **Make sure to keep regular backups** of your modpack and server files.  
While the mod includes safety checks, it cannot cover every possible edge case.

## 👾 Automatic Server Bundle Support

Mods Optimizer automatically disables mods that are **client-only** or **incompatible with servers
**.  
You no longer need to maintain separate `client` and `server` modpacks!

- ✅ One modpack works for both client and server
- 🚫 No more guesswork or duplicate maintenance

## 🧹 Automatic Mod Cleanup

Tired of cleaning out old versions manually?

Mods Optimizer detects and removes **duplicate mod versions** during startup.  
This helps keep your `mods` folder clean and prevents version conflicts.

- 🗑 Removes outdated mod files
- ✅ Keeps only the latest version

## 🚀 How to Use the Mod

1. Add **Mods Optimizer** to your modpack (client and/or server).
2. On game or server startup, it will automatically:
    - 🔍 Scan the `mods` folder
    - 🔄 Classify mods by side (client/server)
    - 🧼 Remove duplicates
    - 🚫 Rename client-only mods on servers to `???.client`

**Tip:** For best results, use this mod on **both client and server**.

## 🧵 Fabric Support?

Currently, **Fabric is not supported**.  
Fabric does not allow mods to run **after the GameProvider but before the Mod Loader**, which
prevents Mods Optimizer from functioning correctly.

> ❗ Support may be added in the future if Fabric changes this behavior.

## 🧪 Example Modpack

Want to see Mods Optimizer in action?  
Check out the BOS Adventure World modpack:  
👉 [BOS Adventure World on CurseForge](https://www.curseforge.com/minecraft/modpacks/bos-adventure-world)

## ℹ️ More Information

For full documentation, usage tips, and FAQs:  
📖 Visit the [Mods Optimizer Wiki](https://github.com/MarkusBordihn/BOs-Mods-Optimizer/wiki)

[logo]: Common/src/main/resources/logo.png
