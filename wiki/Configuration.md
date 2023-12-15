# Configuration

The configuration file `config/mods_optimizer/mods.toml` allows you to define the client and server
side for specific mods.
Furthermore is allows you to enable the debug mode for easier troubleshooting and testing.

## Classify mods

You can edit the config file `config/mods_optimizer/mods.toml` and add mod ids to classify them for
the client and server side.

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

Depending on the mod you can use `client`, `server` or `default` to define the correct side for the
mod.

- `client` means that the mod is physically loaded on the client side.
- `server` means that the mod is physically loaded on the server side.
- `default` means that the mod is physically loaded on the client and server side.

## Debug mode

The debug mode allows you to see which mods are classified for the client and server side with more
details.
It's recommended to enable the debug mode if you are creating a mod pack.

### Force Server or Client side

You can force the Mods Optimizer side to the client or server side by adding using
the `debugForceSide` parameter.

```toml
[Debug]
debugEnabled = "true"
debugForceSide = "client"
```

By using `server` all client side mods will be disabled and renamed to `???.client`.
By using `client` former disabled client mods will be enabled again.
By using `default` the Mods Optimizer will use the default behavior.
