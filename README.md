# Guardian Banner

**Guardian Banner** is a Minecraft Forge 1.20.1 server-friendly protection and territory mod by Vesca Gaming.

It provides configurable protection zones around Guardian Banners, owner/trusted-player permissions, hostile-mob attraction, a barrier system with HP, a management GUI, progressive claim expansion, network synchronization, and optional integration points for modpacks.

## Features

- Guardian Banner block and item
- Configurable protection radius and claim progression
- Owner and trusted-player permissions
- Active / paused barrier state
- Barrier HP with configurable maximum and progression
- Protection against configured hostile threats inside active claims
- Hostile mob attraction around active banners
- Configurable mob-attraction limits and tick intervals
- Minimum distance between main Guardian Banners
- Claim synchronization to clients
- Localized client claim-border rendering
- Management GUI with barrier status
- Server-side configuration
- Entity-type tag to exclude mobs from attraction
- Xaero compatibility hook
- Forge GameTests
- GitHub Actions build workflow

## Requirements

- Minecraft **1.20.1**
- Minecraft Forge **47.4.x**
- Java **17**

The project uses ForgeGradle and the Gradle wrapper included in the repository.

## Build

On Windows:

```powershell
.\gradlew.bat clean
.\gradlew.bat build
```

On Linux/macOS:

```bash
./gradlew clean
./gradlew build
```

The built mod JAR is generated under:

```text
build/libs/
```

## GameTests

Run the Forge GameTest server with:

```powershell
.\gradlew.bat runGameTestServer
```

The project includes automated tests covering:

- claim creation and lookup
- initial barrier HP
- local claim lookup and distance filtering

GameTest structures are stored in:

```text
gameteststructures/
```

## Configuration

Guardian Banner uses a server configuration file:

```text
config/guardianbanner-server.toml
```

This keeps gameplay-related settings server-side and makes the mod easier to use in multiplayer and modpacks.

Important settings include:

- barrier base HP
- HP gained per level
- hostile-mob attraction limits
- mob-attraction tick interval
- client claim-border synchronization radius
- claim progression values

Always review the generated TOML file after the first server launch.

## Entity Attraction Exclusion

Mod developers and modpack authors can exclude entity types from Guardian Banner hostile-mob attraction with the following tag:

```text
#guardianbanner:ignore_attraction
```

The tag is provided at:

```text
src/main/resources/data/guardianbanner/tags/entity_types/ignore_attraction.json
```

## Multiplayer and Performance

Guardian Banner performs claim lookups locally around players instead of scanning every claim on every synchronization tick.

Client claim-border synchronization is throttled, and hostile-mob attraction is processed at a configurable interval with a configurable per-tick cap.

These mechanisms are intended to reduce unnecessary server and network work on multiplayer servers.

## Modpack Compatibility

Guardian Banner is designed to be used as a server-side gameplay mod in Forge 1.20.1 modpacks.

When adding it to a modpack:

1. Use a compatible Forge 1.20.1 version.
2. Review `guardianbanner-server.toml`.
3. Check interactions with other territory/protection mods.
4. Add entity types to the attraction-exclusion tag when required.
5. Test the final modpack on a dedicated server before public deployment.

## Project Structure

```text
src/main/java/com/vescagaming/guardianbanner/
├── block/
├── blockentity/
├── claim/
├── client/
├── menu/
├── network/
├── GuardianBannerConfig.java
├── GuardianBannerMod.java
└── GuardianBannerTags.java

src/main/resources/
├── META-INF/
├── assets/
└── data/

gameteststructures/
└── empty.snbt
```

## License

Guardian Banner is distributed under the MIT License.

See [LICENSE](LICENSE).

## Contributing

Contributions, bug reports, and compatibility feedback are welcome.

When reporting a bug, please include:

- Minecraft version
- Forge version
- Guardian Banner version
- Java version
- relevant crash report or log
- steps to reproduce the issue
- installed mods when the issue involves mod compatibility

For pull requests, keep changes focused and maintain compatibility with Minecraft Forge 1.20.1.

## Disclaimer

Guardian Banner is an independent community project and is not affiliated with Mojang Studios or Microsoft.

Minecraft is a trademark of Microsoft Corporation.

## Credits

**Vesca Gaming**

Guardian Banner is developed as an independent Forge mod project.
