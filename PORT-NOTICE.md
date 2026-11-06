# Port Notice / 移植声明

This repository is an **unofficial community port** of PVPUtils. It is not affiliated with or
endorsed by the original author.

本仓库是 PVPUtils 的**非官方社区移植版**，与原作者无隶属关系，也未获原作者背书。

## Upstream

- Original project: PVPUtils by Nachoneko_miao
- Upstream repository: https://github.com/bakabaicai/PVPUtils
- Port baseline (upstream commit/tag): `v1.9-beta.4`
- Mod version in this branch: `1.9-beta.4`
- Target Minecraft version of this branch: see `gradle.properties` (`minecraft_version`)

## What has been modified

Modifications relative to the upstream baseline are limited to compatibility work needed to run
the same code base on the Minecraft version of this branch:

1. Version-specific Minecraft/Fabric API adaptation (mapping and API signature differences,
   render pipeline changes, mixin injection point adjustments).
2. `CameraMixin` / `RenderStateShardAccessor` / renderer mixins adjusted for the target version's
   class hierarchy and method descriptors.
3. Build configuration adjusted for the target version (Loom, Fabric API, loader versions), and
   `useLegacyMixinAp = true` added where required so the Mixin refmap is generated and injected
   correctly at runtime.
4. All IRC functionality has been removed. Upstream ships IRC as PAHP-protected binaries without
   source, which cannot be redistributed in source form. This port contains **no IRC code and no
   PAHP payloads**; the corresponding UI entries are removed as well.

No changes were made to the license, to the attribution of the original author, or to the
non-commercial nature of the project.

## License

This repository keeps the upstream license unchanged:
**PVPUtils Source-Available Non-Commercial License v1.0** — see [LICENSE](./LICENSE).

In short, and without replacing the license text: attribution to the original author must be
retained, modifications must be disclosed (this file), complete source must be provided, and
commercial use is not permitted. If you redistribute a build of this port, keep this notice and
the original LICENSE together with it.

## Third-party components

- Cadence (MIT License, Copyright (c) 2026 FPSMaster Team) — included as a git submodule.
- Other bundled libraries and their notices: see [docs/THIRD_PARTY_NOTICES.md](./docs/THIRD_PARTY_NOTICES.md)
  and `docs/LICENSES/`.

## Branches

Each branch is named after the Minecraft version it targets (for example `1.21.10`, `1.21.9`).
Built artifacts for convenience live under `dist/` on the matching branch.
