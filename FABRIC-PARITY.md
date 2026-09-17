# Reaching Fabric/NeoForge parity

What is actually left, measured against the tree rather than estimated. Companion to
[MULTILOADER.md](MULTILOADER.md), which covers how the build is put together.

## Where we are

| | count |
|---|---|
| `:common` (loader-agnostic) | 250 |
| `:neoforge` `src/main` | 836 |
| `:neoforge` `src/client` | 311 |
| items registered on Fabric | 60 of ~123 |
| blocks / parts / block entities on Fabric | 0 |

Of the 836 files in `:neoforge/src/main`, only **178 import a loader API**. The other **658 are
blocked purely by depending on those 178.** Parity is therefore not 836 files of work; it is ~178
files of abstraction, after which most of the rest follows.

The Fabric jar ships **no data pack at all** (`processResources` excludes `data/**`), because AE2's
tags name blocks only NeoForge registers and a dangling tag reference aborts registry loading rather
than being skipped. That exclusion is the single clearest signal of how far there is to go: it comes
off when blocks register.

## Does the Fabric side of each API exist for 26.2?

Checked against `fabric-api 0.157.0+26.2` by listing the nested modules, not assumed.

| NeoForge API | AE2 usage | Fabric equivalent | 26.2? |
|---|---|---|---|
| `DeferredRegister` | 10 registers | direct `Registry.register` | **confirmed** (items already done) |
| `neoforge.capabilities` | 57 `BlockCapability`, 7 `ItemCapability`, 3 `EntityCapability` | `fabric-api-lookup-api-v1` (`BlockApiLookup`, `ItemApiLookup`) | **confirmed** |
| `neoforge.transfer` item/fluid | `ItemResource` 23, `FluidResource` 13, `Transaction` 18 | `fabric-transfer-api-v1` (`Storage<ItemVariant>`, `Transaction`) | **confirmed** |
| `neoforge.transfer.energy` | `EnergyHandler`, 6 sites, all FE interop | `teamreborn:energy:5.0.0` | **confirmed** (see below) |
| `neoforge.attachment` | `AttachmentType` | `fabric-data-attachment-api-v1` | **confirmed** |
| payload networking | 37 packet classes | `fabric-networking-api-v1` (`PayloadTypeRegistry`) | **confirmed** |
| `IMenuTypeExtension.create` | `MenuTypeBuilder`, 2 call sites | **nothing, anywhere** | **gap** |
| `NeoForge.EVENT_BUS` | server/player/level/chunk events | `fabric-lifecycle-events-v1`, `fabric-entity-events-v1`, `fabric-events-interaction-v0` | **confirmed** |
| `ModConfigSpec` | `AEConfig` | none; AE2 writes its own | n/a |
| `neoforge.model.data` | model data for cable bus | `fabric-renderer-api-v1` | **confirmed**, different shape |
| datagen providers | full datagen suite | `fabric-data-generation-api-v1` | **confirmed**, but see below |
| chunk loading tickets | `ChunkLoadingService`, spatial anchors | no direct equivalent | **gap** |

### Energy — solved, and smaller than it looked

**AE2's own power system is self-contained.** `IEnergyService`, `IAEPowerStorage` and the energy grid
import nothing from NeoForge. The `neoforge.transfer.energy` usage is confined to six files and is
entirely *Forge Energy interop* — `ForgeEnergyAdapter` is literally the adapter, plus the FE P2P
tunnel, the energy acceptor part, powered items and a debug generator.

So this never blocked AE2's own mechanics; it blocks other mods pushing power into AE2.

The Fabric equivalent is [**`teamreborn:energy:5.0.0`**](https://github.com/TechReborn/Energy) from
`https://maven.fabricmc.net/`, which is built on the same API Lookup and Transaction substrate as
`fabric-transfer-api-v1`, so it maps onto the same abstraction the item and fluid work needs.

    repositories { maven { url = 'https://maven.fabricmc.net/' } }
    dependencies { implementation 'teamreborn:energy:5.0.0' }

`EnergyStorage.SIDED.registerForBlockEntity(...)` and `EnergyStorage.ITEM` correspond to AE2's
`Capabilities.Energy.BLOCK` and `.ITEM` registrations; `SimpleEnergyStorage` replaces the hand-rolled
handler in `ForgeEnergyAdapter`.

**Verified, not assumed:** its POM asks for `fabric-transfer-api-v1:7.0.0` while 26.2 ships `8.0.12`,
a major bump. Gradle resolves 7.0.0 → 8.0.12 and a probe using `EnergyStorage` and
`SimpleEnergyStorage` compiles clean against it. That proves the surface AE2 would touch survives the
bump; it does **not** prove runtime behaviour, so exercise it in the client gametest before trusting
it.

### Menus — no API exists, so AE2 supplies its own

`MenuType implements IMenuTypeExtension`: NeoForge injects that onto the vanilla class. Vanilla's own
`openMenu(MenuProvider)` carries no data and `MenuType` takes a plain `(containerId, inventory)`
supplier, so there is nothing to fall back to. `fabric-screen-handler-api-v1` exists on the Fabric
maven but is **absent from the 0.157.0+26.2 bundle**, and no other module has taken the capability
over — checked module by module across all 44 nested jars.

The coupling turned out to be two lines in one file, so it is now behind
[`appeng.platform.MenuPlatform`](common/src/main/java/appeng/platform/MenuPlatform.java):
`createMenuType` and `openMenu`, both vanilla-typed. NeoForge implements it as a pass-through, so
nothing changed there. The Fabric implementation is deliberately left throwing, with the intended
design written out in its javadoc: send the extra data as an AE2 payload first, then call vanilla
`openMenu`, and have the menu supplier consume a client-side stash.

That rests on the payload being handled before the open-screen packet. Both travel the same
connection and are dispatched to the client thread in arrival order, so it should hold — but it is an
assumption about scheduling rather than a guarantee, and it is precisely what the client gametest
should assert once a menu exists to open.

### Chunk loading

The remaining gap, and a small one: spatial anchors force-load chunks through NeoForge's ticket
controller. Fabric has no equivalent, so this needs a direct `ServerLevel` ticket implementation.

## Order of work

Dependency order, not preference. Each stage unblocks the next; doing them out of order means
writing code against an interface that is about to change.

**1 — Registry plumbing** (`init/`, `core/definitions/`, 16 + 31 files)
Generalise what `FabricItems` already does to blocks, block entities, entity types, data components,
recipe types and serializers, structures. This is mechanical and has no design choices left in it —
`AECommonItems`/`CreativeTabSink` set the pattern. Ends with the `data/**` exclusion coming off, which
is the first moment the Fabric build has recipes and tags.

**2 — `AEComponents` and the key/storage API** (`api/` 162 files)
`AEKey`, `GenericStack`, `ContainerItemStrategies`. Named as the blocker by 20+ entries in
`AECommonItems.notYetPortable()`. Mostly vanilla-typed already; the coupling is `DataComponentType`
registration (stage 1) plus capability lookups.

**3 — Capabilities → `fabric-api-lookup`** (25 files directly, ~90 behind)
`InternalInventory` and `BaseInternalInventory` are the hinge: 663 and 648 files respectively reach
them. Define AE2's own lookup surface in `:common` and register it per loader.

**4 — Transfer** (45 files)
Item and fluid storage against `Storage<ItemVariant>`/`Storage<FluidVariant>`. The transaction models
differ in shape but not in intent — NeoForge's `SnapshotJournal` maps onto Fabric's
`SnapshotParticipant`. Energy is deferred to the decision above.

**5 — Networking and menus** (37 + 78 files)
The menu-data decision is made and the seam is in place; this stage is filling in
`FabricMenuPlatform` and porting the payload registrations.

**6 — The grid** (`me/` 57, `parts/` 72, `blockentity/` 47, `block/` 47)
The bulk, but by this point almost entirely vanilla-typed; expect most of the 658 transitively
blocked files to fall out here without individual attention.

**7 — Client** (311 files, untouched)
Rendering, screens, models. Deliberately last: it is the most loader-bound and least useful before
the server side works. Mixins mostly target vanilla classes and should port with a Fabric mixin
config; `neoforge.client` events (`RegisterPictureInPictureRenderers`, particle providers, render
pipelines) each need a Fabric counterpart.

**8 — Datagen**
Currently NeoForge-only, with output committed to `common/src/generated` and shipped in both jars.
That is fine indefinitely; porting the providers to `fabric-data-generation-api-v1` is optional and
should not block parity.

## How to work on it

**Measure, never estimate.** Import analysis is wrong in both directions here. Move everything into
`:common`, move back what fails to compile, repeat until it converges — that is the only number worth
trusting. Two of the three coupling axes (access transformers, and methods NeoForge patches onto
vanilla classes) are invisible to any import scan.

**Keep the ratchet.** `AECommonItems.notYetPortable()` maps every unported item to the reason it is
blocked, and `AECommonItemsTest` fails if an item appears in neither that map nor the registered
table — so nothing can be silently forgotten. Extend the same pattern to blocks, parts and block
entities as each stage starts. It is the cheapest parity tracker available and it lives in CI.

**Gate every stage on the client gametest.** `:fabric:runClientGametest` boots a client, creates a
world and asserts what registered. Creating the world is the point: it starts the integrated server,
which is the only thing that loads recipes, tags and loot tables. A stage is done when that passes
with the new content registered, not when it compiles.

**NeoForge must stay green.** `:neoforge:test` is 520 tests and is the regression net for everything
moved into `:common`. Run it on every change; a file moving to `:common` is a refactor and must not
alter behaviour.

**Watch for the class-loader trap.** Anything added to `:common` must be inside the `mods { }`
source-set registration, or it loads outside NeoForge's transforming class loader and fails at
runtime while the build stays green.

## What "1:1" will not include

Worth agreeing up front, because each is a decision rather than a task:

- **Mod integrations.** JEI (26 files), Jade (9), WTHIT, EMI, REI — their Fabric builds are separate
  artifacts with separate APIs, and none currently resolves for 26.2.
- **GuideME's guide UI**, until GuideME's own Fabric side registers content. Its `:common` is 82 of
  284 files; AE2's guide integration is 13 files and cannot move before that.
- **The 3d scene export** in GuideME, stubbed because it captured geometry through `MultiBufferSource`.
- **Forge Energy interop**, until stage 4 wires `teamreborn:energy` in. AE2's own power works without
  it; what is missing is other mods pushing power into AE2.
