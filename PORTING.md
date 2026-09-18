# Porting checklist

Working document for getting Fabric to parity. [FABRIC-PARITY.md](FABRIC-PARITY.md) is the research
behind it — read that once, work from this.

## The loop

Four commands, fastest first. Run the cheap ones constantly and the expensive one at each stage gate.

| | what it proves | time |
|---|---|---|
| `python3 tools/parity_report.py` | where the code sits and what is blocking the rest | instant |
| `./gradlew :neoforge:test` | **nothing regressed** — 520 tests, the net under every file moved to `:common` | ~30 s |
| `./gradlew build` | all three modules compile and both jars assemble | ~1 min |
| `./gradlew :fabric:runClientGametest` | Fabric actually **works**: boots a client, creates a world, asserts registrations, screenshots the hotbar | ~30 s |

`JAVA_HOME=/usr/lib/jvm/java-25-openjdk` for all of them. If spotless complains the "JVM-local cache
is stale", `rm -rf .gradle/configuration-cache`.

The gametest is the one that matters. It creates a world, which starts the integrated server — the
only thing that parses recipes, tags and loot tables, so it is the only check that exercises them on
Fabric at all. It also writes `fabric/run-gametest/screenshots/`, which is worth actually looking at:
a missing model renders as the purple-and-black placeholder and Minecraft only whispers about it in
the log.

### Current baseline

Everything below is measured, not remembered. Re-run the report rather than trusting these.

```
:common 258   :neoforge/main 833   :neoforge/client 311
blocked by an import 615   blocked invisibly 218
gametest: 139 items + 49 blocks registered, 139 models resolved, 0 recipes, creative tab present
```

## Discipline

**Keep the ratchet.** `AECommonItems.notYetPortable()` maps every unported item to *why*, and
`AECommonItemsTest` fails if an item is in neither that map nor the ported table. Nothing can be
silently forgotten. **Extend the same pattern to blocks, block entities and parts as each stage
starts** — it is the cheapest parity tracker there is and it runs in CI.

**Anything added to `:common` must be inside the `mods { }` source-set registration**, or it loads
outside NeoForge's transforming class loader and fails at runtime while the build stays green.

**Moving a file to `:common` is a refactor.** If `:neoforge:test` count changes, something else
happened too.

---

## Stage 1 — Registries

Generalise what `FabricItems` does to the rest of the content. Mechanical; the pattern is set.

- [x] Blocks — `AECommonBlocks` + `AECommonBlocksTest`, mirroring `AECommonItems`.
      **49 of 94 registered**, each with its `BlockItem`; the other 45 are in `notYetPortable()`,
      almost all waiting on a block entity. Needed two things beyond transcription: `AEBaseBlock`
      had the same `CreativeModeTab.Output` problem `CreativeTabSink` already solved for items
      (8 overriders updated), and `StairBlock`'s constructor is `protected`, so `AEStairBlock`
      subclasses it to get the reach without an access transformer.
- [ ] Block entity types
- [ ] Entity types
- [ ] Data component types (`AEComponents`) — named by 3 `notYetPortable` entries
- [ ] Recipe types and serializers
- [ ] Structures (`StructurePieceType`, `StructureType`)
- [ ] Attachment types → `fabric-data-attachment-api-v1`
- [ ] Register the 8 custom item-model element types (`ae2:color`, `ae2:storage_cell_state`,
      `ae2:energy_fill_level`, `ae2:facade`, `ae2:memory_card_identity`, `ae2:meteorite_compass`,
      `ae2:color_applicator`, `ae2:portable_cell_color`) — 48 item models currently fail to parse
      on Fabric without them

**Done when:** `processResources` in `fabric/build.gradle` drops `exclude 'data/**'` and the
gametest reports a non-zero recipe count.

## Stage 2 — Components and the key/storage API

- [ ] `AEComponents`
- [ ] `AEKey`, `AEItemKey`, `AEFluidKey`, `GenericStack`
- [ ] `ContainerItemStrategies`
- [ ] Clear the `notYetPortable` entries naming components and the storage API (~20)

**Done when:** storage cells and view cells register and the gametest sees them.

## Stage 3 — Capabilities → `fabric-api-lookup-api-v1`

The hinge: `InternalInventory` and `BaseInternalInventory` are reached by 663 and 648 files.

- [ ] AE2-side lookup surface in `:common`, registered per loader
- [ ] `InternalInventory` / `BaseInternalInventory` off `neoforge.capabilities`
- [ ] `InitCapabilityProviders` equivalent on Fabric
- [ ] Part capabilities (`RegisterPartCapabilitiesEventInternal`)

## Stage 4 — Transfer

- [ ] Item storage → `Storage<ItemVariant>`
- [ ] Fluid storage → `Storage<FluidVariant>`
- [ ] Transactions: NeoForge `SnapshotJournal` → Fabric `SnapshotParticipant`
- [ ] Forge Energy interop → `teamreborn:energy:5.0.0`
      (`maven.fabricmc.net`; resolves and compiles against 26.2, runtime unverified — assert it in
      the gametest). Six sites, all interop; AE2's own power is self-contained and needs nothing.

## Stage 5 — Networking and menus

- [ ] 37 payload types → `fabric-networking-api-v1`
- [ ] Fill in `FabricMenuPlatform` — the seam exists; the design is in its javadoc
- [ ] **Assert the packet-ordering assumption in the gametest.** The design sends menu data as a
      payload immediately before vanilla's open-screen packet and relies on the client handling them
      in that order. It should hold; it is not guaranteed.

## Stage 6 — The grid

`me/` 57, `parts/` 72, `blockentity/` 47, `block/` 47 — plus most of the 217 invisibly-blocked files,
which should fall out here without individual attention.

- [ ] Grid services, pathing, storage/crafting services
- [ ] Parts and the cable bus
- [ ] Chunk loading: spatial anchors need a direct `ServerLevel` ticket implementation, as Fabric has
      no ticket-controller equivalent

## Stage 7 — Client (311 files)

Last on purpose: most loader-bound, least useful before the server side works.

- [ ] Screens and widgets
- [ ] Block entity and part renderers
- [ ] Cable bus model + `neoforge.model.data` → `fabric-renderer-api-v1`
- [ ] Mixins — mostly target vanilla, need a Fabric mixin config
- [ ] Client events: picture-in-picture renderers, particle providers, render pipelines

## Stage 8 — Datagen (optional)

Currently NeoForge-only with output committed to `common/src/generated` and shipped in both jars.
That is fine indefinitely. Port to `fabric-data-generation-api-v1` only if it starts to hurt.

---

## Explicitly out of scope

Decisions, not tasks:

- **Mod integrations** — JEI (26 files), Jade (9), WTHIT, EMI, REI. Separate Fabric artifacts with
  separate APIs; none currently resolves for 26.2.
- **The guide UI**, until GuideME's own Fabric side registers content.
- **GuideME's 3d scene export**, stubbed because it captured geometry through `MultiBufferSource`.
