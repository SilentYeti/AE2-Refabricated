# AE2: Refabricated — Agent Setup & Project Specification

## Purpose

This document describes how to create **AE2: Refabricated**, an unofficial community-maintained fork of **Applied Energistics 2 (AE2)**.

The goal is to take the AE2 codebase targeting **Minecraft 26.1.2** and port it to **Minecraft 26.2**, while providing builds for both:

- Fabric
- NeoForge

The primary purpose of this fork is to restore/maintain **Fabric support** for AE2 while still providing a NeoForge build from the same multiloader project.

This is an **unofficial fork**. It must never be presented as an official Applied Energistics 2 release or as being endorsed by the AE2 development team.

---

# 1. Upstream Project

Upstream project:

**Applied Energistics 2**

Repository:

https://github.com/AppliedEnergistics/Applied-Energistics-2

The starting point for this project should be the AE2 source corresponding to the **Minecraft 26.1.2** release/codebase.

Do not start from an unrelated AE2 version unless explicitly instructed.

The initial port should preserve as much of the existing AE2 implementation as practical while adapting it to the Minecraft 26.2 APIs and loader environments.

---

# 2. Project Identity

The project should be named:

**AE2: Refabricated**

Suggested repository name:

```text
AE2-Refabricated
```

Suggested project description:

> An unofficial community fork of Applied Energistics 2 for Minecraft 26.2, providing Fabric and NeoForge support.

The project is **not**:

- an official AE2 release;
- an official Fabric continuation of AE2;
- affiliated with or endorsed by the Applied Energistics 2 development team.

The project description, README, CurseForge page, Modrinth page, and release notes should make this distinction clear.

Do not name the project simply `Applied Energistics 2`.

Do not imply that the project is maintained by the upstream AE2 team.

---

# 3. Target Architecture

The desired project is a multiloader project.

Conceptually:

```text
AE2: Refabricated
│
├── Common
│   └── Shared AE2 implementation
│
├── Fabric
│   └── Fabric-specific implementation
│
└── NeoForge
    └── NeoForge-specific implementation
```

The exact Gradle/module layout should follow the existing AE2 architecture where practical.

Do not unnecessarily rewrite the entire project architecture merely to make it look different.

Prefer adapting AE2's existing multiloader architecture to Minecraft 26.2.

---

# 4. Loader Targets

The project must produce two separate mod builds.

## Fabric

Target:

```text
Minecraft 26.2
Fabric Loader
Fabric API / required Fabric dependencies
```

The Fabric build is the primary reason this fork exists and should receive first-class support.

It must not be treated as a secondary or experimental port.

## NeoForge

Target:

```text
Minecraft 26.2
NeoForge
```

The NeoForge implementation should remain functional alongside the Fabric implementation.

Do not remove NeoForge support simply because Fabric is the project's primary advertised feature.

---

# 5. Versioning

The project should use a version scheme that clearly identifies the Minecraft version being targeted.

For example:

```text
26.2.x
```

or another versioning scheme consistent with AE2's existing release conventions.

Do not pretend a Refabricated release is an upstream AE2 release.

For example, avoid release names such as:

```text
AE2 26.2.1
```

if that could reasonably be interpreted as an official upstream AE2 release.

Prefer something such as:

```text
AE2: Refabricated 26.2.1
```

or:

```text
AE2 Refabricated 26.2-1.0.0
```

The exact scheme can be chosen during implementation, but it must clearly distinguish Refabricated releases from upstream AE2 releases.

---

# 6. Git History

The project should retain appropriate upstream attribution.

If possible, begin from the appropriate AE2 Git history rather than copying the source into an empty repository.

Preferred approach:

```bash
git clone https://github.com/AppliedEnergistics/Applied-Energistics-2.git
cd Applied-Energistics-2
```

Then check out the appropriate 26.1.2 source/release.

Create the Refabricated branch/repository from that state.

If the project is being created from an exported ZIP rather than a Git clone, initialize Git and make the first commit while clearly documenting the upstream source/version.

The project should maintain an upstream remote when practical:

```text
upstream -> Applied Energistics 2
origin   -> AE2: Refabricated
```

This makes future upstream comparison and porting easier.

---

# 7. Important Git Rule

Do not commit generated build output.

Do not commit:

```text
build/
.gradle/
out/
run/
```

or other generated artifacts unless the upstream project specifically requires them.

Use the upstream `.gitignore` as the starting point.

---

# 8. Licensing

AE2 is a multi-license project.

**Do not treat the entire project as a single LGPL-3.0 work.**

The upstream AE2 26.1.x README identifies the following licensing structure:

| Component | License |
|---|---|
| AE2 source code | LGPL-3.0 |
| AE2 API | MIT |
| Text/translations | CC0 |
| Textures/models | CC BY-NC-SA 3.0 |
| Guidebook click sound | CC BY 4.0 |

Always inspect the exact upstream `README`, `LICENSE`, `NOTICE`, and relevant asset attribution files for the specific source version being used.

Do not remove upstream copyright or license notices.

---

# 9. Source Code License

AE2's source code is licensed under the **GNU Lesser General Public License v3.0**.

The Refabricated source code derived from AE2 must continue to comply with the LGPL.

Do not replace the upstream LGPL licensing with:

```text
All Rights Reserved
```

or another incompatible license.

The project must retain the relevant LGPL license text and copyright notices.

Modified portions should be identifiable as modifications where required by the LGPL.

---

# 10. API License

The AE2 API is licensed under the **MIT License**.

Preserve the applicable MIT copyright/license information when redistributing or modifying the API.

Do not assume the API's MIT license changes the license of the rest of AE2.

---

# 11. Textures and Models

AE2 textures and models are separately licensed under:

**Creative Commons Attribution-NonCommercial-ShareAlike 3.0**

This is extremely important.

Do not treat AE2 textures/models as LGPL code.

Do not remove their attribution.

Do not relicense them under a more permissive license.

Do not assume that changing the code license changes the asset license.

If Refabricated continues using AE2-derived textures/models, the applicable CC BY-NC-SA requirements must remain satisfied.

If a texture/model is substantially replaced with a new original asset, document that appropriately.

---

# 12. Sounds and Other Third-Party Assets

Do not assume every resource file is owned by AE2 or covered by the same license.

The upstream project identifies at least the guidebook click sound as a separately licensed asset:

```text
Guidebook click sound
EminYILDIRIM
CC BY 4.0
```

Preserve the appropriate attribution/license information.

Before publishing, audit third-party assets rather than blindly applying one license to the entire `resources` directory.

---

# 13. Recommended License Layout

The repository should make the multi-license nature of the project obvious.

Recommended structure:

```text
AE2-Refabricated/
├── LICENSE
├── NOTICE
├── LICENSES/
│   ├── LGPL-3.0.txt
│   ├── MIT.txt
│   ├── CC-BY-NC-SA-3.0.txt
│   └── CC-BY-4.0.txt
│
├── common/
├── fabric/
└── neoforge/
```

The exact directory structure can be adjusted to match AE2's existing structure.

The important requirement is that users can easily determine which license applies to which component.

---

# 14. NOTICE File

Create a `NOTICE` file containing attribution to the upstream project and third-party assets.

A starting point can look like:

```text
AE2: Refabricated
=================

AE2: Refabricated is an unofficial fork of
Applied Energistics 2.

Original project:
Applied Energistics 2

Original repository:
https://github.com/AppliedEnergistics/Applied-Energistics-2

AE2 source code:
Licensed under GNU LGPL v3.0.

AE2 API:
Licensed under the MIT License.

AE2-derived textures and models:
Licensed under CC BY-NC-SA 3.0.

Text and translations:
Licensed under CC0.

Guidebook click sound:
EminYILDIRIM
Licensed under CC BY 4.0.

AE2: Refabricated is not affiliated with or endorsed by
the Applied Energistics 2 development team.
```

Before final publication, verify all copyright-holder names and asset attributions against the exact upstream version being used.

Do not invent copyright-holder information.

---

# 15. README

The README should prominently explain the project's relationship to AE2.

Suggested opening:

```markdown
# AE2: Refabricated

An unofficial community fork of Applied Energistics 2 for Minecraft 26.2,
providing both Fabric and NeoForge support.

AE2: Refabricated is based on the Applied Energistics 2 codebase targeting
Minecraft 26.1.2 and adapts it for Minecraft 26.2.

This project is independently maintained and is not affiliated with,
endorsed by, or an official continuation of the Applied Energistics 2 project.

## Original Project

Applied Energistics 2:
https://github.com/AppliedEnergistics/Applied-Energistics-2

See `LICENSE` and `NOTICE` for licensing and attribution information.
```

The disclaimer should remain visible rather than being hidden deep inside the README.

---

# 16. Do Not Misrepresent Upstream

Never use wording such as:

```text
Official AE2 Fabric
```

```text
Official Fabric continuation
```

```text
AE2 26.2 official port
```

unless explicit permission has been obtained from the upstream maintainers to make such a claim.

Preferred wording:

```text
Unofficial AE2 fork
```

```text
Community-maintained AE2 fork
```

```text
AE2 port maintained independently from the upstream project
```

---

# 17. Porting Strategy

The goal is a functional Minecraft 26.2 port, not an architectural rewrite.

Start by getting the common project compiling against Minecraft 26.2.

Then address loader-specific compilation.

General order:

```text
1. Update Minecraft version
2. Update mappings
3. Update Gradle/dependency versions
4. Resolve common compilation/API changes
5. Resolve Fabric-specific changes
6. Resolve NeoForge-specific changes
7. Resolve resources/data changes
8. Launch development clients
9. Test dedicated servers
10. Test both loaders independently
```

Do not make large unrelated refactors while performing the initial port.

Keep changes focused so that future upstream synchronization remains practical.

---

# 18. Handle Minecraft API Changes Carefully

Minecraft version ports commonly involve changes to:

- registries;
- networking;
- rendering;
- item/block APIs;
- data components;
- serialization;
- tags;
- recipes;
- menus/screens;
- world/chunk APIs;
- entity APIs;
- resource loading;
- client/server separation;
- mappings.

When adapting AE2 code, prefer the closest 26.2 equivalent of the existing implementation.

Do not introduce unnecessary abstractions unless they solve an actual cross-loader problem.

---

# 19. Multiloader Rule

Common code should contain logic that can genuinely be shared.

Loader-specific APIs should remain in their respective loader modules.

Conceptually:

```text
common/
    AE2 logic
    blocks/items
    networking abstractions
    storage logic
    crafting logic

fabric/
    Fabric registration
    Fabric networking
    Fabric-specific hooks
    Fabric entrypoint

neoforge/
    NeoForge registration
    NeoForge networking
    NeoForge-specific hooks
    NeoForge entrypoint
```

Avoid importing Fabric classes into common code.

Avoid importing NeoForge classes into common code.

The common module must remain loader-independent.

---

# 20. Fabric Is a First-Class Target

Fabric is the primary advertised feature of Refabricated.

Therefore:

- Fabric must be tested independently.
- Fabric development runs must work.
- Fabric dedicated-server operation must work.
- Fabric client operation must work.
- Fabric dependencies must be correctly declared.
- Fabric-specific code must not be treated as temporary hacks if a proper implementation is possible.

Do not produce a nominal Fabric jar that merely compiles but does not function.

---

# 21. NeoForge Must Remain Functional

NeoForge should be maintained as a legitimate supported target.

At minimum test:

```text
NeoForge client
NeoForge dedicated server
```

and verify that core AE2 functionality remains operational.

The goal is:

```text
                 AE2: Refabricated
                       │
             ┌─────────┴─────────┐
             │                   │
          Fabric              NeoForge
             │                   │
          26.2.x              26.2.x
```

rather than a Fabric port with a broken NeoForge compatibility layer.

---

# 22. Build Outputs

The build should produce distinct artifacts for each loader.

For example:

```text
build/libs/
├── ae2-refabricated-26.2.x-fabric.jar
└── ae2-refabricated-26.2.x-neoforge.jar
```

The exact filenames can follow the upstream Gradle conventions.

Do not upload development/debug artifacts as release files.

---

# 23. Dependencies

Clearly declare loader-specific dependencies.

The Fabric release should declare the required Fabric dependencies.

The NeoForge release should declare the required NeoForge dependencies.

Do not package dependencies inside the AE2 jar unless the upstream build system explicitly requires it.

Avoid accidental dependency duplication.

---

# 24. Modrinth Publishing

Create a **separate Modrinth project** for AE2: Refabricated.

Do not attempt to upload Refabricated releases to the existing upstream AE2 project.

The project should clearly identify itself as a fork.

Modrinth permits license-compliant forks that have substantially diverged from the original project.

Refabricated qualifies as a genuine fork/port because it modifies the AE2 codebase to target a new Minecraft version and maintains its own loader implementations.

When creating the Modrinth project:

- Identify Applied Energistics 2 as the upstream project.
- Use the derivative-content disclosure mechanism.
- Clearly state that Refabricated is an unofficial fork.
- Link the upstream repository.
- Preserve all applicable licenses and attribution.
- Do not imply official AE2 endorsement.

The project should list both:

```text
Fabric
NeoForge
```

as supported loaders.

---

# 25. CurseForge Publishing

Create a **separate CurseForge project**.

Do not attempt to replace or impersonate the official AE2 CurseForge project.

The project should have its own:

- name;
- icon;
- description;
- project page;
- release files.

CurseForge's rules permit forks when the original license permits the fork and the original creator is credited/linked.

The project should therefore:

1. Identify AE2 as the original project.
2. Link the AE2 repository.
3. Clearly identify Refabricated as an unofficial fork.
4. Use a distinct project identity.
5. Preserve the applicable licenses.
6. Avoid using misleading official AE2 branding.

---

# 26. CurseForge License Setting

Because the project contains components under multiple licenses, do not represent the entire project as simply:

```text
LGPL-3.0
```

if doing so would imply that the textures/models are LGPL.

Use the appropriate custom/multi-license description supported by CurseForge.

Explain that:

```text
Source code       -> LGPL-3.0
API               -> MIT
Textures/models   -> CC BY-NC-SA 3.0
Text/translations -> CC0
Other assets      -> Individual licenses in NOTICE
```

The repository's `LICENSE`/`NOTICE` files should be authoritative and complete.

---

# 27. Modrinth/CurseForge Project Branding

Use an original project icon if possible.

The safest branding strategy is to make the project visually distinct from the official AE2 project while clearly identifying its relationship to AE2.

Do not simply copy the upstream project avatar and pretend the project is official.

Recommended project title:

```text
AE2: Refabricated
```

Recommended subtitle:

```text
Unofficial AE2 fork for Minecraft 26.2 with Fabric and NeoForge support.
```

---

# 28. Release Description

Every release should identify the fork relationship.

Example:

```markdown
## AE2: Refabricated 26.2.x

Minecraft 26.2 release of AE2: Refabricated.

This release is an unofficial community-maintained fork of
Applied Energistics 2.

### Loaders

- Fabric
- NeoForge

### Based On

Applied Energistics 2, Minecraft 26.1.2 codebase.

### Important

This project is not affiliated with or endorsed by the
Applied Energistics 2 development team.
```

---

# 29. Testing Requirements

Before publishing a release, test both loaders.

## Fabric Client

Test:

- startup;
- world creation;
- placing AE2 blocks;
- breaking AE2 blocks;
- ME network creation;
- channels;
- terminals;
- storage;
- crafting;
- import/export;
- wireless functionality;
- rendering;
- GUIs;
- JEI/REI integration if supported;
- world save/load.

## Fabric Server

Test:

- dedicated server startup;
- joining from Fabric client;
- placing/breaking AE2 blocks;
- ME networks;
- chunk loading;
- automation;
- server shutdown/restart;
- world persistence.

## NeoForge Client

Run the same functional tests.

## NeoForge Server

Run the same functional tests.

---

# 30. Porting Completion Criteria

Do not consider the port complete merely because Gradle produces a jar.

The minimum completion criteria are:

```text
[ ] Common module compiles
[ ] Fabric module compiles
[ ] NeoForge module compiles

[ ] Fabric client launches
[ ] Fabric server launches

[ ] NeoForge client launches
[ ] NeoForge server launches

[ ] AE2 blocks/items register correctly
[ ] AE2 data loads correctly
[ ] AE2 GUIs work
[ ] ME networks work
[ ] Storage works
[ ] Crafting works
[ ] Automation works
[ ] World saves persist correctly
[ ] Client/server separation is correct

[ ] No obvious upstream license files were removed
[ ] NOTICE is present
[ ] Third-party asset attribution is preserved
[ ] README identifies the project as an unofficial fork
```

---

# 31. Upstream Synchronization

Keep the upstream repository available as a Git remote when possible.

Example:

```bash
git remote add upstream https://github.com/AppliedEnergistics/Applied-Energistics-2.git
```

The Refabricated repository remains:

```bash
git remote -v
```

with:

```text
origin    -> Refabricated repository
upstream  -> Applied Energistics 2
```

Do not automatically merge upstream changes into the 26.2 branch.

Review changes manually.

The goal is to make future porting easier without accidentally importing incompatible changes.

---

# 32. Changes Should Be Clearly Separated

When practical, organize commits into logical categories:

```text
Port Minecraft 26.1.2 -> 26.2
Port Fabric implementation
Port NeoForge implementation
Fix networking API changes
Fix rendering API changes
Fix data component changes
Fix server compatibility
Update dependencies
```

Avoid giant commits containing unrelated changes.

This makes debugging and future upstream synchronization substantially easier.

---

# 33. Do Not Remove AE2 Attribution

Do not:

- remove upstream copyright notices;
- remove the upstream license;
- replace AE2 attribution with Refabricated attribution;
- claim that all AE2 code was written by Refabricated;
- remove asset attribution;
- remove third-party licenses.

Your own contribution should be added **alongside** upstream attribution.

---

# 34. Refabricated Contributions

Your own modifications may be documented separately.

For example:

```text
AE2: Refabricated modifications:
Copyright (c) 2026 AE2: Refabricated contributors
```

However, do not use this to imply ownership of upstream AE2 code or assets.

The distinction should remain:

```text
Original AE2 work
        +
Refabricated modifications
        =
AE2: Refabricated
```

---

# 35. Asset Audit Before First Release

Before publishing version 1.0, perform an asset audit.

Inspect:

```text
src/main/resources/
```

and identify:

- textures;
- models;
- sounds;
- fonts;
- guidebook content;
- third-party libraries/assets;
- other files that may have independent licensing.

For each non-code asset, determine:

```text
Who created it?
What license applies?
Does attribution need to be included?
Does the license permit redistribution?
Does the license impose NonCommercial or ShareAlike requirements?
```

Do not assume the repository's main source-code license applies to every resource.

---

# 36. Important Commercial-Use Consideration

AE2-derived textures/models are licensed **CC BY-NC-SA 3.0**.

The `NC` restriction must be respected.

Do not assume that being hosted on a monetized platform automatically grants permission for commercial use of those assets.

If the project ever intends to introduce commercial use, sponsorship arrangements, paid downloads, or other monetization involving AE2-derived assets, review the applicable asset licenses first.

---

# 37. If Assets Are Replaced

If Refabricated eventually creates completely new textures/models:

```text
AE2 original assets
        ↓
new Refabricated assets
```

document the new assets separately.

Do not copy AE2 asset files and then simply label them as original Refabricated assets.

A clean long-term goal could be:

```text
Code:
AE2-derived LGPL code

Assets:
Refabricated-original assets
```

but this is optional and should not be undertaken merely to complicate the initial port.

---

# 38. Development Priority

The project's development priority should be:

```text
1. Functional Minecraft 26.2 port
2. Functional Fabric support
3. Functional NeoForge support
4. Correct client/server behavior
5. Correct licensing/attribution
6. Compatibility with the existing AE2 ecosystem
7. Documentation
8. Platform publishing
```

Do not prioritize cosmetic changes over basic functionality.

---

# 39. Agent Behavior

When an agent is working on this repository:

### Do

- inspect existing AE2 code before changing it;
- preserve upstream architecture where practical;
- search for the Minecraft 26.2 replacement for deprecated APIs;
- keep Fabric and NeoForge implementations separated;
- test after significant changes;
- preserve licensing;
- preserve attribution;
- document significant deviations from upstream;
- keep commits logically organized.

### Do not

- rewrite AE2 unnecessarily;
- delete license files;
- remove attribution;
- assume all assets use the source-code license;
- make Fabric code leak into common code;
- make NeoForge code leak into common code;
- claim the project is official;
- upload Refabricated builds to the official AE2 project pages;
- publish before both loaders have been tested.

---

# 40. Definition of Done

AE2: Refabricated is ready for public release when:

```text
Minecraft 26.2
        │
        ├── Fabric ──────── functional
        │
        └── NeoForge ────── functional
```

and:

```text
Source
  ├── LGPL obligations satisfied
  ├── MIT API attribution preserved
  ├── CC BY-NC-SA assets correctly attributed/licensed
  ├── CC0 material preserved appropriately
  └── CC BY assets correctly attributed

Repository
  ├── README
  ├── LICENSE
  ├── NOTICE
  └── third-party license information

Publishing
  ├── Separate Modrinth project
  ├── Separate CurseForge project
  ├── Fork relationship disclosed
  ├── Upstream AE2 linked
  └── No implication of official endorsement
```

The resulting project should be recognizable as an independent, community-maintained AE2 fork while retaining proper attribution to the original project and complying with the licenses attached to each portion of the work.