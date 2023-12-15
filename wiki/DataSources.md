# Data Sources

The mod attempts to extract as much information as possible directly from its files.

If that proves unfeasible, the mod then resorts to retrieving information from an internal list of
recognized mods.
The determination of whether mods are client or server side primarily relies on my personal
experience, mod packs I've created, insights from the community, and information gathered from
CurseForge and Modrinth.

## CurseForge

I manually utilize the CurseForge website to obtain information about mods and check their source
code, if available.
For mods that are not open source, I generally rely on the community to provide information about.

See: https://www.curseforge.com/minecraft/mc-mods

## Modrinth

Utilizing the Modrinth API provides a way to access details about mods designated as purely client
or server side.
Unfortunately, the API does not furnish the mod id for such mods, requiring a manual process to
obtain the correct mod id and subsequently add it to the internal list of recognized
mods.

See: https://modrinth.com/mods

### Client Side Query

For querying client side-only mods, I generally employ the following API request:
https://api.modrinth.com/v2/search?game=minecraft&project_type=mod&loader=forge&sort=created_at&limit=100&offset=0&server_side=unsupported

It's important to note that the query is constrained to 100 results, necessitating the use of
the `offset` parameter to retrieve all results.
Additionally, it's conceivable that this example query may become obsolete in the future.
For the most up-to-date information, please refer to the Modrinth API documentation available
at https://docs.modrinth.com/api.
