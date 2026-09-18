# Porting checklist

Working document for getting Fabric to parity. [FABRIC-PARITY.md](FABRIC-PARITY.md) is the research
behind it and [MULTILOADER.md](MULTILOADER.md) explains how the build is put together — read those
once, work from this.

## Starting cold

```sh
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk        # 25 exactly; 21 will not do

# GuideME is a REQUIRED dependency of AE2 and needed its own 26.2 port. Publish it first or
# nothing here resolves.
git clone https://github.com/SilentYeti/GuideME-Refabricated.git
(cd GuideME-Refabricated && ./gradlew :neoforge:publishToMavenLocal -Pversion=26.2.0-ae2port)

./gradlew build
```

`:fabric:runClientGametest` launches a real Minecraft client, so it needs a display. It closes
itself; expect a window for ~20 seconds.

## The loop

Four commands, fastest first. Run the cheap ones constantly and the expensive one at each stage gate.

| | what it proves | time |
|---|---|---|
| `python3 tools/parity_report.py` | where the code sits and what is blocking the rest | instant |
| `./gradlew :neoforge:test` | **nothing regressed** — the net under every file moved to `:common` | ~30 s |
| `./gradlew build` | all three modules compile and both jars assemble | ~1 min |
| `./gradlew :fabric:runClientGametest` | Fabric actually **works**: boots a client, creates a world, asserts registrations, screenshots the hotbar | ~30 s |

`JAVA_HOME=/usr/lib/jvm/java-25-openjdk` for all of them. If spotless complains the "JVM-local cache
is stale", `rm -rf .gradle/configuration-cache`.

**On Windows**, point `JAVA_HOME` at a JDK 25 and use `gradlew.bat`; both tools run there too. If
`spotlessCheck` fails on a file you have not touched, the working tree has CRLF line endings — a
checkout made with `core.autocrlf=true` or copied in from elsewhere. The index is LF and git reports
nothing to commit, so re-check the files out rather than editing them:
`git ls-files --eol | grep w/crlf` lists them; delete and `git checkout --` them.

The gametest is the one that matters. It creates a world, which starts the integrated server — the
only thing that parses recipes, tags and loot tables, so it is the only check that exercises them on
Fabric at all. It also writes `fabric/run-gametest/screenshots/`, which is worth actually looking at:
a missing model renders as the purple-and-black placeholder and Minecraft only whispers about it in
the log.

Counts are deliberately not written down here — they go stale within a commit or two, and this
document has already drifted once. `tools/parity_report.py` and the gametest's own log lines are the
answer to "where are we".

## How to find what can move

**Never by reading imports.** There are three coupling axes and only one of them is visible that way:

1. imports of `net.neoforged.*` — visible
2. **access transformers** — invisible. AE2 widens 56 vanilla members and `:common` has none, so
   anything touching one fails to compile there
3. **methods NeoForge patches onto vanilla classes** — invisible. Called with no NeoForge import at
   all (`GuiGraphicsExtractor.peekScissorStack`, `submitGuiElementRenderState`)

An import-based estimate of what could move said 34 files. Moving them and compiling said 233. So:

```sh
python3 tools/try_move_to_common.py appeng/recipes --dry-run   # what would stick, and why the rest would not
python3 tools/try_move_to_common.py appeng/recipes             # actually do it
```

It moves, compiles, moves back whatever failed, repeats until it settles, and prints each bounced
file with the compiler error that is the real reason it cannot move. Then run the rest of the loop
above before committing.

**Relocation alone is exhausted until a seam changes something.** Before a change, `--all --dry-run`
moves every file and every one bounces. Each file then needs a deliberate change first: break a
cycle, split state from its event, narrow an interface, or resolve something from a registry instead
of naming a definitions class. **Run `--all --dry-run` again after every such change** — it is the
cheapest way to see what the change opened up, and it puts everything back. The payoff can be much
larger than the change: taking one method off `InternalInventory` let 39 unrelated files cross.

The tail of its output is the other half of the answer: every bounced file with the compiler error
that stopped it. Aggregating those errors (`package X does not exist`, grouped and counted) ranks
the loader APIs still holding files back, which is how to choose the next seam.

**Expect moves in bursts, and measure couplings instead.** Most of `:neoforge` is one cycle, and
upstream's API is not the clean layer it looks like: `GridHelper`, `PartHelper`, `StorageHelper` and
`PatternDetailsHelper` are facades that call straight into the implementation. So the API cannot
cross ahead of the grid it fronts, and a seam that removes a coupling from the middle of the cycle
often moves nothing on its own. That is progress anyway. The number to watch is the count of files
in the cycle that still bounce on a *loader* package; relocation happens when it reaches zero.

**The report's "order of attack" is a set cover, not a ranking.** Its cumulative numbers answer "if I
fixed these groups, in this order" — which is not the same question as "what is holding the most
back". Two other numbers, straight out of `parity_report.analyse()`, are more useful when choosing:

```python
import sys; sys.path.insert(0, "tools")
import parity_report as pr, collections
_, _, profile, invisible = pr.analyse()
need, alone = collections.Counter(), collections.Counter()
for bs, n in profile.items():
    for g in bs: need[g] += n
    if len(bs) == 1: alone[next(iter(bs))] += n
```

`need` counts the files a group has to be cleared for; `alone` counts the files it would free *by
itself*. As of the component seam: **123 files need exactly one group cleared** (`model` 35,
`transfer` 25, `capabilities` 12, `network` 11), and **~220 need seven or more** — that second
group is the grid, the block entities and the client, and no single seam touches it. Aim at the
first group; the second falls out of stages 6 and 7 or not at all.

Also worth keeping in view: the same run counts **309 files blocked with no loader import at all**,
more than any single import group. Those are the invisible axes, and until the component seam nothing
had been done about any of them. A bounced file's error is where to start — most say `cannot find
symbol` because something *else* bounced, but the ones naming a vanilla member are real, and the
`CustomData.contains` row below is what dealing with one looks like.

## Patterns that already work

Reach for these before inventing something; each is in the tree with a comment explaining itself.

| problem | answer | example |
|---|---|---|
| a vanilla member is `protected` and only reachable via AT | subclass it — a subclass may call a protected super constructor | `AEStairBlock` |
| a vanilla member is `private` and subclassing cannot reach it | on Fabric, an **access widener** — Loom's counterpart to an access transformer | none yet; `ItemModels.ID_MAPPER` will be the first |
| an API takes an AT-widened type | define a narrow interface in `:common` and adapt per loader | `CreativeTabSink` for `CreativeModeTab.Output` |
| loader-agnostic code needs state that only an event can set | the state moves to `:common`, the event handler stays with its loader | `WrenchDisassembly` / `WrenchHook` |
| `:common` needs something only a loader can answer | add to the platform SPI — vanilla-typed signatures only | `AEPlatform`, `MenuPlatform` |
| a helper is a thin wrapper over something vanilla already has | reimplement it in `:common` | `AEStreamCodecs` for `NeoForgeStreamCodecs`, `AERecipeType` for `RecipeType.simple` |
| two classes name each other across the boundary | invert it — the leaf owns the constant, the table points at the leaf | recipe classes own their `RecipeType`; `AERecipeTypes` collects them |
| data is in a loader's format | translate it while assembling the other jar, and fail the build on anything unrecognised | `neoforge:conditions` → `fabric:load_conditions` |
| an API interface method returns a loader type, implemented differently per class | take it off the interface; a static helper in the loader module dispatches on the concrete type. A test pins every answer. Leave the loader-side interface for the classes that really are the loader's, and for addons | `NeoForgeInventories`, pinned by `NeoForgeInventoriesTest`; `ResourceHandlerProvider` for `PlatformInventoryWrapper` |
| a loader patches a convenience method onto a vanilla class | call the vanilla long way round, and pin on the loader's side that the two agree | `CustomData.contains` → `copyTag().contains`, pinned by `CustomDataTest` |
| a class caches a loader object by identity | keep the cache, but as an opaque slot the loader fills | `BaseInternalInventory.getOrCreatePlatformAdapter` |
| a Fabric seam has no reachable caller yet | throw `UnsupportedOperationException` naming the stage, and write the intended design in the javadoc. Returning "nothing" would look like working content that silently does nothing | `FabricMenuPlatform`, `FabricItemTransferPlatform` |
| a static initializer registers AE2's own loader-specific defaults | the SPI supplies them; keep the call in the static initializer so the ordering guarantee survives | `StackWorldBehaviorsPlatform` |
| content cannot be registered on Fabric yet | declare it in an `AECommon*` table, or explain it in `notYetPortable()` | `AECommonItems`, `AECommonBlocks` |

## Discipline

**Keep the ratchet.** `AECommonItems.notYetPortable()` maps every unported item to *why*, and
`AECommonItemsTest` fails if an item is in neither that map nor the ported table. Nothing can be
silently forgotten. **Extend the same pattern to blocks, block entities and parts as each stage
starts** — it is the cheapest parity tracker there is and it runs in CI.

**A new SPI must load through its own class loader** -- `ServiceLoader.load(X.class, X.class.getClassLoader())`, never the one-argument overload. That one uses the calling thread's context loader, which under a mod loader is not reliably the one that loaded AE2; when it is not, the implementation is defined a second time and its first reference back into AE2 throws `LinkageError`, depending only on which thread touched the SPI first. All six read the same way now.

**Anything added to `:common` must be inside the `mods { }` source-set registration**, or it loads
outside NeoForge's transforming class loader and fails at runtime while the build stays green.

**Moving a file to `:common` is a refactor.** If the `:neoforge:test` count changes, something else
happened too.

**Raise the gametest floors when they rise.** `MIN_RECIPES` in `AE2ClientGameTest` is a floor, not a
target; leaving it low lets a regression hide under it.

**`:common` may not import `net.neoforged.*` or `net.fabricmc.*`.** Nothing enforces this but the
compiler — `:common` builds against vanilla only, so a stray import simply fails.

---

## Stage 1 — Registries

Generalise what `FabricItems` does to the rest of the content. Mechanical; the pattern is set.

- [x] Blocks — `AECommonBlocks` + `AECommonBlocksTest`, mirroring `AECommonItems`.
      **49 of 94 registered**, each with its `BlockItem`; the other 45 are in `notYetPortable()`,
      almost all waiting on a block entity. Needed two things beyond transcription: `AEBaseBlock`
      had the same `CreativeModeTab.Output` problem `CreativeTabSink` already solved for items
      (8 overriders updated), and `StairBlock`'s constructor is `protected`, so `AEStairBlock`
      subclasses it to get the reach without an access transformer.
- [ ] Block entity types — needs the block entity classes, which need the grid. **Stage 6, not here.**
- [ ] Entity types — one entity, `TinyTNTPrimedEntity`, and it is genuinely loader-coupled: it
      implements NeoForge's `IEntityWithComplexSpawn` for its extra spawn data and calls `EventHooks`.
      Fabric has no drop-in for either, so this needs a custom spawn packet. Small feature, poor
      return; do it when the networking seam exists (stage 5)
- [ ] Data component types (`AEComponents`) — named by 3 `notYetPortable` entries
- [ ] Recipe types and serializers — **groundwork done, the move is blocked on a cycle.**
      `AERecipeTypes` and `AERecipeSerializers` are plain ordered tables now rather than
      `DeferredRegister`s, registered through `AppEngBase`'s existing `RegisterEvent` path, so their
      shape is already loader-agnostic. `EntropyRecipe`, `InscriberRecipe` and `ChargerRecipe` have
      lost their last tie to `:neoforge`.
      **The cycle is broken and 7 more files crossed**, four of them real recipe classes: Entropy,
      Charger, Inscriber and MatterCannon, with their builders. The recipe classes own their
      `RecipeType` now and `AERecipeTypes` collects them, instead of the two naming each other.
      Two things had to be emulated on the way. `RecipeType.simple` turned out to be a NeoForge
      addition to the vanilla interface — invisible, since calling it needs no import — so
      `AERecipeType.simple` is the same three lines somewhere both loaders can reach. And display
      icons that named `AEItems`/`AEBlocks` now resolve from the item registry by id, which is the
      right place to ask anyway.
      **Four types and serializers now register on Fabric** — inscriber, entropy, matter cannon and
      charger — through `AECommonRecipes`, the same subset-table pattern as `AECommonItems`, with
      `AECommonRecipesTest` as its ratchet. Recipes went **148 to 178**.
      Still in `:neoforge`: `AERecipeTypes` and `AERecipeSerializers` themselves (they name classes
      that have not crossed), `TransformRecipe` (quantum bridge), `QuartzCuttingRecipe` and
      `TransformLogic` (`neoforge.common`, the event bus), and the upgrade/facade recipes.
- [x] **Recipe load conditions.** AE2 gates 67 matter-cannon recipes on "this tag is not empty", so
      they only load when another mod supplies the tag. Both halves are handled now:
      *Data* — `fabric/build.gradle` translates `neoforge:conditions` into Fabric's
      `fabric:load_conditions` while assembling the jar, so the data is generated once in NeoForge's
      form and converted on the way in. The translator understands only the shapes AE2 emits and
      **fails the build on anything else**, because dropping a condition it did not recognise would
      silently load a recipe with an empty ingredient tag.
      *Code* — the datagen builders that construct the condition moved from `MatterCannonAmmo` to
      `MatterCannonAmmoProvider`, where their only caller already lived; datagen runs on NeoForge
      only, so that is where they belong.
      Confirmed working in game: matter-cannon recipe errors fell from 68 to 4, and the 4 that remain
      are the ones whose tags vanilla does populate (iron, gold, copper) plus the one unconditional
      recipe — exactly the set that *should* still try to load.
- [x] Structures (`StructurePieceType`, `StructureType`) — **6 of the 9 `appeng/worldgen` files
      crossed; the remaining 3 are a stage 6 cycle.**
      The package's *only* tie to NeoForge was `net.neoforged.neoforge.common.Tags`, in two files,
      for four biome tags. Those turn out to be plain `c:` convention tags — NeoForge's
      `Tags.Biomes` and Fabric's `ConventionalBiomeTags` are two names for the same tag IDs — so
      naming them on AE2's own `ConventionTags` (which already had `c:is_ocean` and the
      `biomeTag` helper) removes the import outright rather than abstracting over it. Verified
      that `fabric-convention-tags-v2` ships all four data files, since a missing one would not
      error, it would silently make every meteorite fall back to `DEFAULT` fallout.
      Across: `PlacedMeteoriteSettings`, `MeteoriteSpawner`, and all four `Fallout*` classes.
      Still in `:neoforge`: `MeteoritePlacer`, `MeteoriteStructure` and `MeteoriteStructurePiece`,
      as one cycle — the piece calls `ServerCompassService`, which needs `AEBlocks` and
      `MysteriousCubeBlockEntity`. **Block entities, so stage 6**, same gate as the block entity
      types above. Nothing loader-specific is left in any of the three; `MeteoriteStructure` lost
      its last one (`AppEng.makeId` → `AEConstants.makeId`) on the way.
      Registration of the `StructureType`/`StructurePieceType` on Fabric waits on that same cycle
- [ ] Attachment types → `fabric-data-attachment-api-v1`. Exactly one attachment,
      `AEAttachmentTypes.HOLDING_CTRL`, a per-player boolean. Both loaders have the concept under
      different APIs, so it wants a two-method seam (`isHoldingCtrl` / `setHoldingCtrl`) rather than
      a shared type. Its only consumers are `UpdateHoldingCtrlPacket` and `PartPlacement`, neither of
      which is close to crossing, so building the seam now would leave it with nothing to serve —
      do it alongside stage 5

**As far as it goes on its own.** Every item above is either done or refiled with a reason: block
entity types to stage 6, entity and attachment types to stage 5, data components to stage 2, and the
recipe classes that have not crossed to whichever stage owns what they name. Nothing in stage 1 is
still waiting on stage 1 — picking this up means starting at stage 2, not finishing here.

The blanket `exclude 'data/**'` is gone and the data pack ships a directory at a time, because the
kinds of data fail differently: an unparseable *recipe* is logged and skipped, whereas tags, worldgen
and the dynamic registries go through `RegistryDataLoader` where one dangling reference is fatal and
world creation aborts. Verified by lifting them: it crashes on `ae2:meteorite_compass` and on the
vanilla `enchantable/*` tags AE2 contributes to.

Each remaining `exclude` line in `fabric/build.gradle` names what has to register before it can go.
The gametest asserts the recipe count as a floor rather than an equality, so it ratchets up as
serializers land; `AE2ClientGameTest` holds the current number.

**Done when:** every exclusion is gone and the recipe floor reaches AE2's full count — which cannot
happen until the stages those exclusions name are done. Treat stage 1 as closed and come back to
delete exclusion lines as later stages unblock them.

## Stage 2 — Components and the key/storage API

- [x] `AEKey`, `AEItemKey`, `AEFluidKey`, `GenericStack` — **the whole `appeng/api/stacks` package is
      in `:common`**, plus `AEKeyFilter`/`NoOpKeyFilter` that it pulled along.
      `AEFluidKey` was the substantive one: it stored a NeoForge `FluidStack` with the amount pinned
      to 1, which is the same information as a `Holder<Fluid>` plus a `DataComponentPatch`, both
      vanilla. It holds those now — not a compromise shape, since Fabric's `FluidVariant` is exactly
      that pair, so each loader hands the two values over rather than translating. The two things
      vanilla genuinely cannot answer about a fluid, its display name and its default components,
      go through a `FluidPlatform` seam so NeoForge behaviour is preserved rather than approximated.
      Conversions to each loader's resource types moved to that loader: `NeoForgeFluids`,
      `NeoForgeItems`. `AEKeyTypesInternal` dropped NeoForge's `BakeCallback` for a registry-size
      comparison, which is slightly stronger — it sees a key type registered *after* the bake.
      **The NBT and JSON format is unchanged**, covered by the existing codec roundtrip tests, so
      saved worlds are unaffected. The packet format dropped an amount that was always 1.
      Two AE2-internal cycles had to be cut, both by moving ownership rather than adding a seam
      (the same fix as the recipe types): `AEMissingContent` owns the missing-content item and its
      three components, `WrappedStacks` owns carrying a `GenericStack` inside an `ItemStack`, and
      `AEComponents` merely registers both, so registration is still in one place
- [x] `AEComponents` itself — **in `:common`, and all 35 component types register on Fabric.**
      Three of the four things holding it there were not real: a `DeferredRegister`, two javadoc links
      to `AEItems`, and the four `Encoded*Pattern` records, which named `AEItems` only for
      `AEItems.MISSING_CONTENT.is`. `AEMissingContent` already owned that item and only wanted an
      `is(AEKey)` next to its `is(ItemStack)`; with it, the records and `AECodecs` stop naming
      `AEItems` and `AEComponents` at all — `AECodecs` was hand-rolling what
      `AEMissingContent.replacement` already does — and the cycle is gone. It is an ordered table now,
      the same shape as `AERecipeTypes`, through `AppEngBase`'s `RegisterEvent` path on NeoForge and
      `FabricComponents` on Fabric. **The ids are unchanged**, which matters more than it looks: a
      component type under a different id makes every saved stack carrying it unreadable.
      **18 files crossed**, among them the whole `IConfigManager`/`Setting` API and the menu field
      sync. Asserted in the client gametest, because an unregistered component type does not throw
      when an item sets it — it throws later, when a stack carrying it is written out
- [x] The two `notYetPortable` entries that named only components: **`NAME_PRESS` and
      `MISSING_CONTENT`, 139 items to 141.** The missing-content item needed one more thing, and it is
      the first of the *invisible* couplings to be dealt with rather than measured:
      `CustomData.contains` is NeoForge's addition to the vanilla class, so it asks the copied tag
      instead. The branch already re-checked that on the copy, so the dead re-check goes with it, and
      `CustomDataTest` pins that the two are the same question — on the NeoForge side, since that is
      where the patched class is. `MEMORY_CARD`, the third, also needs the parts API
- [ ] `ContainerItemStrategies` — stage 3, it is a capability lookup
- [ ] Register the key types on Fabric, and the `FabricFluids` counterpart to `NeoForgeFluids`
      (needed by stage 4, not before)
- [ ] Clear the `notYetPortable` entries naming components and the storage API (~20)

**Done when:** storage cells and view cells register and the gametest sees them. That is **23 of the
58 unregistered items** — 10 `BasicStorageCell`, 10 `PortableCellItem`, 3 spatial — which is why this
stage is worth more than its file count suggests.

## Stage 3 — Capabilities → `fabric-api-lookup-api-v1`

The hinge: `InternalInventory` and `BaseInternalInventory` are reached by 663 and 648 files.

- [x] AE2-side lookup surface in `:common` — **`AEBlockCapability`**, a handle (id, API type, sided or
      not) with `find` and `createCache`, resolved through `BlockCapabilityPlatform`. `AECapabilities`
      holds handles now. On NeoForge a handle resolves to the *identical* `BlockCapability` the field
      used to hold — NeoForge interns by name — so registration and lookup still meet, pinned by
      `NeoForgeCapabilitiesTest`. `NeoForgeCapabilities.of(handle)` is what registration code passes to
      NeoForge; `.handle(cap)` wraps a NeoForge capability (the P2P tunnels do this with NeoForge's own
      item/fluid/energy ones). Fabric is implemented for real on `BlockApiLookup`/`BlockApiCache`: with
      no providers yet, lookups answer null, as NeoForge does where none exists. Registering AE2's own
      providers on Fabric waits on the block entities (stage 6)
- [x] `IManagedGridNode` off `ValueIOSerializable` — it redeclared both methods and nothing used the
      supertype
- [x] `GenericInternalInventory` into `:common`. Its transaction hook moved to the NeoForge-side
      `TransactionalGenericInventory`, which `GenericStackInv` already satisfies through
      `SnapshotJournal`. A generic inventory from another mod that is not transactional is left
      unexposed to NeoForge's transfer API, with a warning, rather than wrapped in a handler that could
      not roll back an aborted transaction
- [x] `InternalInventory` / `BaseInternalInventory` off `neoforge.capabilities` — **in `:common`**.
      `toResourceHandler()` came off the interface: `NeoForgeInventories.resourceHandler(inv)` gives
      the answer each implementation used to give, the special-case ones via `ResourceHandlerProvider`,
      and `NeoForgeInventoriesTest` pins all of them. `wrapExternal` goes through the new
      `ItemTransferPlatform` SPI, which throws on Fabric until stage 4 — its only callers are the
      inscriber and molecular assembler, neither registered there. **This alone let 39 other files
      cross**, among them `AppEngInternalInventory`, the priority lists and the crafting inventories
- [x] `StackWorldBehaviors`' defaults out of its static initializer and behind
      `StackWorldBehaviorsPlatform`. The strategies that reach another block's inventory are written
      against the loader's transfer API, so the loader names them; the call stays *in* the static
      initializer because `register*` keeps the first registration per key type, which is what makes
      AE2's defaults beat an addon's. `StackWorldBehaviorsDefaultsTest` pins that NeoForge still gets
      items and fluids for import, export and placement -- an empty registry does not throw, it silently
      transfers nothing, so `FabricStackWorldBehaviors` must be filled in *before* the buses and planes
      arrive there
- [x] The inventories with their own NeoForge adapter off `ResourceHandlerProvider` -- **6 more in
      `:common`**. `NeoForgeInventories` recognises each by its concrete type instead, and the inventories
      only expose what the adapter is built from: the menu, the player inventory, the delegate, the
      sub-inventories. `PlatformInventoryWrapper` keeps the interface because it genuinely is NeoForge's --
      it wraps a `ResourceHandler` to begin with -- and so does an addon's own inventory.
      `NeoForgeInventoriesTest` needed no edit: it was already written against `NeoForgeInventories`, which
      is the point of pinning the helper rather than the classes
- [ ] `InitCapabilityProviders` equivalent on Fabric -- **gated on stage 6, not on this stage.** Every
      registration in it names an AE2 block entity or part, and `AECommonBlockEntities` does not exist yet:
      there is nothing on Fabric to hang a provider on. The lookup side is finished and answers null, which
      is what NeoForge answers where no provider exists, so nothing is wrong in the meantime
- [ ] Part capabilities (`RegisterPartCapabilitiesEventInternal`) -- **also gated on stage 6.** The
      loader-agnostic shape is clear: AE2 owns the registry (host types, and providers keyed by
      `AEBlockCapability` and part class), each loader drains it into its own lookup system, and
      `RegisterPartCapabilitiesEvent` stays in `:neoforge` as the mod-bus event addons post into, delegating
      to it. But the registry's own signatures are in terms of `IPart` and `IPartHost`, which are still in
      `:neoforge`, so the `:common` half of that seam cannot be written yet. Do it when the parts API crosses

With those two gated, **stage 3 is done as far as it can go without stage 6**, and `capabilities` has
gone from the largest blocker to the third: of the files that still name it, the ones that are not already
in `appeng/neoforge/` are blocked on `neoforge.transfer` as well, or are item and entity capabilities
(`P2PTunnelAttunement`, `FluidContainerItemStrategy`, `CuriosIntegration`) whose Fabric counterpart is
`ContainerItemContext` -- stage 4, not a block lookup. `transfer` is now the biggest single blocker and the
head of the greedy order, so that is where to go next.

## Stage 4 — Transfer

`fabric-transfer-api-v1` is already on the classpath -- `:fabric` depends on the whole of `fabric-api`, and
8.0.12 resolves for 26.2 -- and it has a counterpart for everything the NeoForge side uses:
`Storage<ItemVariant>` / `Storage<FluidVariant>` for `ResourceHandler`, `ItemStorage.SIDED` /
`FluidStorage.SIDED` (they are `BlockApiLookup`s, so they go through `AEBlockCapability` like any other),
`CombinedStorage`, `PlayerInventoryStorage`, `SnapshotParticipant`, and `ContainerItemContext` for the item
capabilities. So the shape of `FabricInventories.storage(InternalInventory)` is the shape
`NeoForgeInventories.resourceHandler(InternalInventory)` already has, case for case.

- [ ] Item storage → `Storage<ItemVariant>` — `FabricInventories.storage`, mirroring
      `NeoForgeInventories.resourceHandler`'s dispatch, and `FabricItemTransferPlatform.findExternal` over
      `ItemStorage.SIDED`
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

- [ ] The 8 custom item-model element types (`ae2:color`, `ae2:storage_cell_state`,
      `ae2:energy_fill_level`, `ae2:facade`, `ae2:memory_card_identity`, `ae2:meteorite_compass`,
      `ae2:color_applicator`, `ae2:portable_cell_color`). **Moved here from stage 1**: 48 item models
      fail to parse on Fabric without them, but the classes behind them
      (`ColorApplicatorItemModel`, `FacadeItemModel`, `MeteoriteCompassModel`,
      `EnergyFillLevelProperty`, the tint sources) all live in `neoforge/src/client`, so nothing can
      register them until this stage moves.
      There is a second problem waiting behind that one. NeoForge registers them through
      `RegisterItemModelsEvent`, `RegisterRangeSelectItemModelPropertyEvent` and
      `RegisterColorHandlersEvent`, which reach `ItemModels.ID_MAPPER` — `private static final` in
      vanilla *and* in the patched jar. Fabric's answer is an **access widener**, its counterpart to
      an access transformer; Loom supports them natively and `:fabric:validateAccessWidener` already
      runs (as NO-SOURCE, because there is no widener yet). That is the first one this port needs
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
