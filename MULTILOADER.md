# AE2: Refabricated — multiloader notes

This repository is **AE2: Refabricated**, a fork of Applied Energistics 2 on Minecraft 26.2
targeting both NeoForge and Fabric. Its required companion is
[GuideME-Refabricated](https://github.com/SilentYeti/GuideME-Refabricated), which needed the same
treatment. The mod id stays `ae2` — only the display name changed.

Both halves are documented here: AE2 first, GuideME in the second half.

`./gradlew build` produces both loaders:

| | jar | run |
|---|---|---|
| NeoForge | `neoforge/build/libs/ae2-refabricated-neoforge-*.jar` | `./gradlew :neoforge:runClient` (also `runServer`, `runData`, `runGuide`, `runGametest`) |
| Fabric | `fabric/build/libs/ae2-refabricated-fabric-*.jar` | `./gradlew :fabric:runClient` (also `runServer`) |

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

`:common` holds the loader-agnostic Java plus **all** of the shared assets and data, hand-written
(`src/main/resources`) and datagen output (`src/generated/resources`) alike. Everything else still
lives in `:neoforge`. The Fabric jar builds, loads, bundles `:common`, resolves the SPI, and
registers 90 items plus AE2's creative tab, with their models and translations. No blocks, parts,
block entities, menus or networks yet.

Datagen output only moved into `:common` after the fact, and that is worth knowing about: it used to
live in `:neoforge`, so the Fabric jar shipped none of AE2's 555 recipes, 407 advancements, 101 loot
tables or 44 tag files -- **and none of its item models**, because the item model definitions under
`assets/ae2/items` are datagen output too. `:neoforge:runData` still produces it; only `--output`
changed.

Formatting is checked from the **root** project, not from `:neoforge` -- spotless rejects targets
outside the project directory, so only the root can see all three modules. Do not move it back.

## The item port

60 of AE2's 118 item declarations (90 registry entries, counting the two colored paint ball
families) now run on both loaders. They are declared in `AECommonItems` in `:common`; `:fabric`
registers them directly, since Fabric has no deferred registry phase.

`:fabric:runClientGametest` is what checks this, and it is the only automated test on the Fabric
side: it boots a client, asserts the items are registered and that none of them fall back to the
missing-model placeholder, then creates a world -- which is the only way to exercise a data pack --
and screenshots AE2 stacks in the hotbar. It earned its place immediately by catching
`METEORITE_COMPASS`, which registers fine and then has no model, because its model needs a custom
`ItemModel` type that only NeoForge can register.

### Why the Fabric jar carries no data pack

A missing tag reference is not skipped: it fails registry loading, which aborts world creation. AE2's
tag files name the blocks that only `:neoforge` registers, so shipping `data/**` to Fabric turns a
working client into one that cannot load a world at all. `:fabric`'s `processResources` excludes it,
and pulls the shared resources through itself rather than copying `:common`'s output straight into
the jar, so that the exclusion applies to a dev run too. Assets ship in full.

Along the way: 48 of the 365 item model definitions fail to parse on Fabric with
`Unknown element id: ae2:color` and friends. Those are AE2's own custom `ItemModel` types, registered
from `:neoforge` client code through `RegisterItemModelsEvent`; vanilla keeps `ItemModels.ID_MAPPER`
private, so Fabric needs an access widener plus those model classes out of `:neoforge`. Every one of
them belongs to an item Fabric does not register yet, so it is noise rather than breakage.

`AEItems` itself stays in `:neoforge` and is unchanged. It cannot move yet, and the reason is worth
writing down because it is *the* thing blocking the rest of the mod:

    ItemDefinition -> AEItemKey / GenericStack -> AEComponents -> AEItems -> every item class
                                                                         -> blocks, parts, menus, grid

`AEItems` imports all 44 item classes and `AEKey`/`GenericStack` import `AEItems` back, so items,
blocks, block entities, parts, menus and the grid are all one 559-file cycle with 94 files touching
a NeoForge API. Measured: sealing the 14 highest-value classes in that cycle moves only 18 of the 44
item classes, so **there is no small cut** -- the seams have to be built.

Two coupling axes turned out to be one-line fixes rather than abstractions, and both are now gone:

* `ConventionTags` only needed NeoForge for its `c:` tag constants. The ids are spelled out and
  `ConventionTagsTest` pins all 26 against `Tags.Items`/`Tags.Blocks`/`Tags.Biomes`.
* `AEBaseItem.addToMainCreativeTab` named `CreativeModeTab.Output`, which is `protected` in vanilla
  and only widened by the access transformer. It takes `CreativeTabSink` now. `CreativeModeTabs`'
  own tab constants are private for the same reason, so `AECommonItems` spells those keys out too,
  pinned by `AECommonItemsTest`.

`AECommonItems.notYetPortable()` names the seam each of the remaining 58 items is waiting on, and
`AECommonItemsTest` fails if an item is added to `AEItems` without being ported or listed. Grouped by
seam -- the portable cells need two, so the counts add up to more than 57:

| seam | items waiting | what it needs |
|---|---|---|
| storage cells (`ICellHandler`, `StorageCells`) | 25 | the storage API, which is item-handler shaped: 10 basic cells, 10 portable cells, view/creative cell, 3 spatial cells |
| energy (`IAEItemPowerStorage`, `AEBasePoweredItem`) | 16 | an energy abstraction over `neoforge.transfer` and Fabric's own energy API: 10 portable cells, 4 powered tools, 2 wireless terminals |
| `Upgrades` / `UpgradeCardItem` | 9 | reaches `IPartHost` -> `IPart` -> `neoforge.model.data.ModelData` |
| `PatternDetailsHelper` | 4 | the crafting pattern API |
| `AEComponents` | 4 | `DeferredRegister` for data component types (2 of these also need the parts API) |
| debug tools | 4 | `AEConfig` on `fml.config`, plus the grid and worldgen |
| menus (`MenuTypeBuilder`) | 3 | menu registration plus the network channel |
| the parts API | 1 | `FacadeItem` |
| client item models | 1 | `METEORITE_COMPASS`, see above |
| GuideME (`GuidesCommon`) | 1 | GuideME's own Fabric module |

The energy and storage seams overlap heavily (portable cells need both), which is why they are the
two to do first.

## Migration order (measured, not guessed)

Counting files that become eligible for `:common` once a dependency is abstracted away. These were
measured before anything moved, so the first row is history; the rest still holds.

| abstract | files eligible for :common |
|---|---|
| nothing | 26 |
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
