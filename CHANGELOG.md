# Changelog

## 1.3.0
- Added owner-only barrier enable/disable button to the Guardian Banner GUI.
- Disabling the barrier pauses mob attraction and siege pressure.
- Re-enabling a destroyed barrier restores it to full HP.
- Added explicit active/siege state to the GUI.
- Improved barrier regeneration logic.
- Added localized server feedback for barrier toggling.
- Bumped project version to 1.3.0.


## 1.2.0

- Added a server-side common TOML configuration.
- Added configurable 100-block minimum banner spacing.
- Added configurable attraction radius and update interval.
- Added configurable barrier health, progression and regeneration.
- Added per-dimension persistent claim storage.
- Improved claim synchronization so clients only receive their dimension's claims.
- Added statistics for attracted mobs, blocked explosions, damage taken and active time.
- Improved activation/deactivation feedback with particles and sounds.
- Reactivating a destroyed banner now restores the full configured barrier immediately.
- Owner-only destruction of the main Guardian Banner.
- Improved explosion and hostile-mob protection configuration.
- Added a GitHub Actions build workflow.
- Added MIT license and release documentation.

## 1.1.2

- Prevented Guardian Banners from being placed within 100 blocks of another Guardian Banner.

## 1.1.1

- Fixed destroyed-barrier reactivation and red-border state.


## 1.4.2

- Corrected Forge GameTest annotation imports for Forge 1.20.1 (`net.minecraftforge.gametest`).

## 1.4.1
- Fixed configurable barrier HP initialization and safe reload clamping.
- Fixed enchantment slot accounting when upgrading existing enchantments.
- Reduced hostile-mob tick workload with configurable intervals.
- Added a per-banner attraction processing cap.
- Limited border synchronization to claims near each player.
- Moved gameplay configuration to server config.
- Added optional entity tag for attraction exclusions.
- Added Forge GameTests for core persistence/lookup and HP initialization.
