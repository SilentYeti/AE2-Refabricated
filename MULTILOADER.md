# AE2: Refabricated — multiloader notes

This repository is **AE2: Refabricated**, a fork of Applied Energistics 2 on Minecraft 26.2
targeting both NeoForge and Fabric. Its required companion is
[GuideME-Refabricated](https://github.com/SilentYeti/GuideME-Refabricated), which needed the same
treatment. The mod id stays `ae2` — only the display name changed.

Both halves are documented here: AE2 first, GuideME in the second half.

`./gradlew build` produces both loaders:

| | jar | run |
|---|---|---|
| NeoForge | `neoforge/build/libs/appliedenergistics2-neoforge-*.jar` | `./gradlew :neoforge:runClient` (also `runServer`, `runData`, `runGuide`, `runGametest`) |
| Fabric | `fabric/build/libs/appliedenergistics2-fabric-*.jar` | `./gradlew :fabric:runClient` (also `runServer`) |

Requires `JAVA_HOME=/usr/lib/jvm/java-25-openjdk`. GuideME must be published to mavenLocal first:

    cd GuideME-Refabricated && ./gradlew :neoforge:publishToMavenLocal -Pversion=26.2.0-ae2port

GuideME is itself a multiloader build now, with the same layout and the same
`:common` / `:neoforge` / `:fabric` split. **It is a real git repo** with two commits: the 26.2 port,
then the restructure. Its `:neoforge` jar bundles `:common`, which is what AE2 compiles against.

## Layout

    ae2-262/
      common/     loader-agnostic code + the platform SPI. Compiled against vanilla only, via
                  ModDevGradle's NeoForm mode (neoform_version). Holds ALL shared assets/data.
      neoforge/   ModDevGradle. The mod as it stands today, plus NeoForgePlatform.
      fabric/     Loom. FabricPlatform + the entrypoint.

Minecraft is unobfuscated from 26.x onward, so all three compile against identical Mojang names and
no remapping is involved. `:common` is never published on its own -- its classes and resources are
bundled into each loader jar.

## The seam

`appeng.platform.AEPlatform` (in `:common`) is the only way loader-agnostic code may ask the loader
anything. Each loader registers an implementation via `META-INF/services`. Keep it small: every
method costs two implementations.

## Three things that bit us, so they don't bite you again

1. **`:common` must be registered as part of the mod**, not merely put on the classpath:

       mods { ae2 { sourceSet project(':common').sourceSets.main } }

   Without this its classes load outside NeoForge's transforming class loader, see untransformed
   copies of the vanilla types, and die with `VerifyError: TagValueOutput is not assignable to
   ValueOutput` at runtime. The build stays perfectly green.

2. **Access transformers are a coupling axis.** `AEBaseItem` compiles fine on NeoForge and not in
   `:common`, because `CreativeModeTab.Output` is `protected` in vanilla and AE2's
   `accesstransformer.cfg` widens it. Anything touching one of those 56 AT entries cannot move to
   `:common` as-is.

3. **Loom needs project repositories**, so `repositoriesMode` had to go from
   `FAIL_ON_PROJECT_REPOS` to `PREFER_PROJECT`, and `:fabric` now declares its own repositories
   (under PREFER_PROJECT a project that declares any stops seeing the settings ones). Loom also
   requires Gradle >= 9.5, so the wrapper moved 9.2.1 -> 9.7.0.

## What is actually ported

`:common` holds 26 files -- the dependency-closed set that needed no abstraction at all. Everything
else still lives in `:neoforge`. The Fabric jar builds, loads, bundles `:common` and the shared
assets, and resolves the SPI; it registers no blocks, items or networks yet.

## Migration order (measured, not guessed)

Counting files that become eligible for `:common` once a dependency is abstracted away:

| abstract | files eligible for :common |
|---|---|
| nothing (today) | 26 |
| all of NeoForge | 34 |
| all of NeoForge **+ GuideME** | **1347** |
| ... + JEI/Jade/EMI/REI | 1392 |

**GuideME is the chokepoint, not NeoForge.** 13 files import it and they transitively hold back
1313 others. Do that first -- either port GuideME to multiloader too, or put AE2's guide integration
behind an interface in `:common`. Only then does abstracting NeoForge pay off.

After GuideME, in descending order of size:

| NeoForge API | imports | files |
|---|---|---|
| `neoforge.transfer` (item/fluid/energy) | 118 | 45 |
| `neoforge.client` (render, models, events) | 95 | 56 |
| `neoforge.common` (tags, config, util) | 50 | 38 |
| `neoforge.model` (model data) | 49 | 42 |
| `neoforge.capabilities` | 35 | 25 |
| `neoforge.event` | 28 | 17 |
| `neoforge.registries` (DeferredRegister) | 22 | 17 |
| `neoforge.network` | 21 | 20 |
| `bus.api` + `fml` | 50 | 47 |
| `neoforge.fluids` | 9 | 8 |

`neoforge.transfer` is the hard one: Fabric has no drop-in equivalent, so it needs a designed
abstraction over both its own API and Fabric's Transfer API, not a mechanical translation.

---

# GuideME-Refabricated

Same layout, same rules. `./gradlew build` makes both jars; `:neoforge:publishToMavenLocal` is what
AE2 consumes.

## Why GuideME went first

AE2 cannot move code into its own `:common` while GuideME is NeoForge-only: 13 AE2 files import
GuideME and transitively hold back 1313 others. GuideME is also far more tractable — 18 of its 284
files import NeoForge, against 235 of AE2's 1392.

## The first real abstraction: fluids

NeoForge's `FluidStack` has no vanilla or Fabric equivalent, and the tint a fluid renders with comes
from `FluidModel.fluidTintSource()` — a field NeoForge **patches onto a vanilla record**. So
`GuideMePlatform.getFluidSprite(Fluid)` returns a plain `TextureAtlasSprite` plus a tint int, and the
caller builds the `Blitter`. The SPI stays purely vanilla-typed, which is the rule that keeps it
implementable on both loaders.

`RenderContext.renderFluid(FluidStack, ...)` is gone. The `Fluid` overload — the one AE2 and other
consumers actually call — is unchanged.

## The coupling axis no import scan can see

`:common` failed to compile on calls like `GuiGraphicsExtractor.peekScissorStack()` and
`.submitGuiElementRenderState(...)`. Those are **NeoForge methods patched onto vanilla classes**:
code calls them with no `net.neoforged` import at all, so import analysis reports those files as
loader-agnostic when they are not. Nine files are affected, concentrated in `SimpleRenderContext`
and `Blitter` — GuideME's GUI core.

This is why the measured "1347 files eligible" figure for AE2 is an upper bound, not a promise.

## Where GuideME stands

82 of 284 files are in `:common`. Converging it took five rounds of moving still-coupled files back
(48 → 68 → 48 → 16 → 7), which shows how far the entanglement reaches: 187 files transitively depend
on the coupled core.

What blocks the remaining 202, in the order worth attacking:

1. **`GuideME` / `GuideMEClient`** — the mod main classes, referenced ~160 times from code that is
   otherwise loader-agnostic (config lookups, `instance()`). Split each into a common core plus a
   thin loader entrypoint and most of the tree follows.
2. **The 9 GUI files** above. Needs a rendering seam, or Fabric-side equivalents of NeoForge's
   `GuiGraphics` extensions.
3. **`GuidebookLevel` / `FakeForwardingServerLevel`** — implement vanilla `Level` interfaces that
   NeoForge extends (`ModelData`, `PartEntity`, `AuxiliaryLightManager`). Genuinely loader-specific;
   expect a common interface with a subclass per loader.
4. **Mixins** — `guideme.mixins.json` is NeoForge-shaped; Fabric needs its own config.

The Fabric jar builds, loads, bundles `:common` and resolves the SPI (including fluid naming and
fluid sprites). It registers and renders no guides yet.
