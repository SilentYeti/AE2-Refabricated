# AE2: Refabricated

A fork of [Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2)
ported to **Minecraft 26.2**, restructured as a **multiloader** build targeting both NeoForge and
Fabric.

The mod id is still `ae2` — it is baked into every asset path, `AppEng.MOD_ID`, and GuideME's
references, so renaming it would break the mod. Only the display name differs.

## Status

| | state |
|---|---|
| NeoForge | **works** — builds, loads, 489 unit tests pass |
| Fabric | **scaffolding** — jar builds and loads, resolves the platform SPI, registers no content |
| Rendering | never verified by drawing a frame |

## Build

    JAVA_HOME=/usr/lib/jvm/java-25-openjdk ./gradlew build

Requires JDK 25. [GuideME](../guideme-262) must be published to mavenLocal first — it is a required
dependency and needed its own 26.2 port:

    cd ../guideme-262 && ./gradlew :neoforge:publishToMavenLocal -Pversion=26.2.0-ae2port

| | jar | run |
|---|---|---|
| NeoForge | `neoforge/build/libs/ae2-refabricated-neoforge-*.jar` | `./gradlew :neoforge:runClient` |
| Fabric | `fabric/build/libs/ae2-refabricated-fabric-*.jar` | `./gradlew :fabric:runClient` |

## Layout

    common/     loader-agnostic code + the platform SPI, compiled against vanilla only (NeoForm)
    neoforge/   ModDevGradle. Everything not yet abstracted.
    fabric/     Loom. Platform implementation and entrypoint.

Minecraft is unobfuscated from 26.x onward, so all three compile against identical Mojang names and
nothing is remapped. `:common` is never published on its own — its classes and the shared
assets/data are bundled into each loader jar.

`appeng.platform.AEPlatform` is the seam. Code in `:common` must never import `net.neoforged.*` or
`net.fabricmc.*`; each loader registers an implementation via `META-INF/services`.

See [MULTILOADER.md](../MULTILOADER.md) for the porting notes, the measured migration roadmap, and
the three coupling axes — two of which no import scan can see.

## Upstream

Baseline is upstream tag `v26.1.11-beta`. `../ae2-26.2-port.patch` is the 26.2 port against that
tag; the multiloader restructure is this repository's history.

## License

Unchanged from upstream: LGPLv3, MIT, CC BY-NC-SA 3.0. See [LICENSE](LICENSE).
