# AE2: Refabricated

A fork of [Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2)
ported to **Minecraft 26.2**, restructured as a **multiloader** build targeting both NeoForge and
Fabric.

The mod id is still `ae2` — it is baked into every asset path, `AppEng.MOD_ID`, and GuideME's
references, so renaming it would break the mod. Only the display name differs.

## Status

| | state |
|---|---|
| NeoForge | **works** — builds, loads, 520 unit tests and 69 game tests pass |
| Fabric | **partial** — registers 90 items (tools, materials, cards, cell components, paint balls) and AE2's creative tab, with their models and translations; no blocks, parts, networks or data pack |
| Rendering | **runs** — `runClient` reaches the menu with every atlas baked and no render-thread errors; no GUI has been driven by hand |
| Guide export | **broken** — see below |

`runGametest` runs 69 tests against a real dedicated server, so registration, recipes, networks and
block entities are exercised, not just compiled.

On Fabric, `:fabric:runClientGametest` boots a client, creates a world and asserts that the items in
[`AECommonItems`](common/src/main/java/appeng/core/definitions/AECommonItems.java) are registered,
have a resolved item model, and appear under AE2's creative tab. The other 58 of AE2's 118 item
declarations each name the seam they are waiting on in `AECommonItems.notYetPortable()`;
`AECommonItemsTest` fails if an item is added to `AEItems` without being either ported or listed
there. See [MULTILOADER.md](MULTILOADER.md#the-item-port) for what is left.

**The Fabric jar deliberately ships no data pack.** AE2's tags name blocks that only `:neoforge`
registers, and a missing tag reference is not skipped -- it fails registry loading and aborts world
creation. So `:fabric` excludes `data/**` (see its `processResources`) until the blocks follow, which
means no AE2 recipes, advancements or loot tables on Fabric yet. Assets are shipped in full, and
have to be: the item model definitions under `assets/ae2/items` are datagen output, so without them
even a registered item has no model.

`runGuideexport` and `createStaticSite` throw: GuideME's `SceneExporter` is a deliberate 26.2 stub
(the 3d scene export relied on `MultiBufferSource`, which 26.2 removed in favour of submit-based
rendering). Fixing that means porting the exporter in GuideME-Refabricated, not here.

## Build

    JAVA_HOME=/usr/lib/jvm/java-25-openjdk ./gradlew build

Requires JDK 25. [GuideME-Refabricated](https://github.com/SilentYeti/GuideME-Refabricated) must be
published to mavenLocal first — it is a required dependency and needed its own 26.2 port:

    git clone https://github.com/SilentYeti/GuideME-Refabricated.git
    cd GuideME-Refabricated && ./gradlew :neoforge:publishToMavenLocal -Pversion=26.2.0-ae2port

| | jar | run |
|---|---|---|
| NeoForge | `neoforge/build/libs/ae2-refabricated-neoforge-*.jar` | `./gradlew :neoforge:runClient` |
| Fabric | `fabric/build/libs/ae2-refabricated-fabric-*.jar` | `./gradlew :fabric:runClient` |

### In IntelliJ

The Gradle build pins the IDEA project SDK to JDK 25, so a Gradle sync is enough for the run
configurations ModDevGradle generates -- they declare no JRE of their own and take the project SDK.

If **Run Client** still fails with `Unrecognized option: --sun-misc-unsafe-memory-access=allow`, the
run is being delegated to a Gradle daemon on an older JDK: set *Settings -> Build, Execution,
Deployment -> Build Tools -> Gradle -> Gradle JVM* to the same JDK 25. The flag comes from NeoForge
and only exists from JDK 24 on, so an older JVM rejects it before Minecraft starts.

`./gradlew :neoforge:runClient` is unaffected by either setting: ModDevGradle gives its own run tasks
the Java toolchain launcher, so it works even when Gradle itself is running on an older JDK.

## Layout

    common/     loader-agnostic code + the platform SPI, compiled against vanilla only (NeoForm)
    neoforge/   ModDevGradle. Everything not yet abstracted.
    fabric/     Loom. Platform implementation and entrypoint.

Minecraft is unobfuscated from 26.x onward, so all three compile against identical Mojang names and
nothing is remapped. `:common` is never published on its own — its classes and the shared
assets/data are bundled into each loader jar.

`appeng.platform.AEPlatform` is the seam. Code in `:common` must never import `net.neoforged.*` or
`net.fabricmc.*`; each loader registers an implementation via `META-INF/services`.

See [PORTING.md](PORTING.md) for the working checklist and how to test,
[FABRIC-PARITY.md](FABRIC-PARITY.md) for the research behind it,
and [MULTILOADER.md](MULTILOADER.md) for the porting notes, the measured migration roadmap, and
the three coupling axes — two of which no import scan can see.

## Upstream

Baseline is upstream tag `v26.1.11-beta`. [`docs/ae2-26.2-port.patch`](docs/ae2-26.2-port.patch) is
the 26.2 port against that tag, kept because this repository's first commit squashes work that
predates it; the multiloader restructure is in the history proper.

## License

Unchanged from upstream: LGPLv3, MIT, CC BY-NC-SA 3.0. See [LICENSE](LICENSE).
