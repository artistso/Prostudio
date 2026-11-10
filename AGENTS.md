# AGENTS.md — instructions for any AI working in this repository

You are working on **ProStudio**. Read this before changing anything. It is the contract for
humans and assistants (Arena.ai Agent Mode, and any other AI) contributing to this tree.

---

## What this project is

**ProStudio is the merger of two of the same author's projects:**

| Ancestor | What it was | What it is now |
|---|---|---|
| **ProCraft** | The original open-source painting app (Rust engine + Kotlin/Compose Android UI + egui desktop). | Renamed to **ProStudio** as of v0.0.5-beta. Old `.procraft` files, plugins and settings carry over; new files use `.prostudio` names. |
| **InkFrame v5** | A web/PWA drawing app whose design — "the Glass Horizon" — is the author's preferred look and feel for drawing software. | Its **design language** (the rose/plum world, the wordmark, the optical orbs, the perimeter board, frosted glass) is being merged into ProStudio's Android UI. |

The merged result, this repository, is **ProStudio with the Glass Horizon interface** — the
engine of ProCraft/ProStudio, the skin and interaction ideas of InkFrame v5.

**The two codesbase roles are binding:**

- **ProStudio's Rust engine and core (`crates/`) are authoritative for drawing, writing
  and the artwork.** They stay exactly as they are, unless a correctness bug is found.
- **InkFrame v5 is the visual and interaction reference only** — what the app *looks like*
  and *how it reacts*, never how pixels are computed. Its source lives in a **separate
  repository**: `github.com/artistso/inkframesv5` (clone it next to this one if you need to
  compare; do not copy code blindly).

Known artefacts of the merge so far (Android only): the Glass Horizon wordmark and
neurorbs, the frosted full-screen pages, the inkFrame glyph set and the perimeter
frame board with its `+` invitation stop, rendered by the Compose UI under
`android/app/src/main/java/io/github/artistso/prostudio/ui/` (`GlassHorizon*`, `GlassNodes.kt`,
`GlassIcons.kt`, `Theme.kt`'s `object Glass`). The desktop (egui) interface has **not** been
re-themed yet.

---

## The InkFrame v5 reference — where it is and what binds

Clone `github.com/artistso/inkframesv5`. Inside it:

- `web/index.html` — the reference implementation (markup, CSS and JS).
- `docs/GLASS_HORIZON_VISUAL_CONTRACT.md` — **the binding visual contract**: palette
  (rose/plum), title, the four-sided perimeter frame board, the assist rail, the eight
  optical nodes, the live lens, the frosted overlays, landscape density.
- `docs/GLASS_HORIZON_COMPLETION_REGISTRY.md` — the `GH-xxx` registry of parity items and
  their status. If you complete or move an item, update its row there (or say why you did not).

Do not "improve on" the contract silently. If InkFrame has no "frame cap" equivalent in
ProStudio, do **not** invent one — adapt honestly (e.g. show a live frame count, not a
fabricated limit) and say so.

---

## Repository layout

| Path | What |
|---|---|
| `crates/engine` | document, brushes, stroke geometry, canvas view (no GPU) |
| `crates/gpu` | wgpu renderer: layers, strokes, compositing, display |
| `crates/core` | what Android and desktop share: canvas session, every touch and gesture, tools' state, gallery, presets, time-lapse, exports, shortcuts; with the Android app's UniFFI bindings |
| `android` | the Android app (Gradle, Jetpack Compose) — the platform the merge is happening on |
| `desktop` | the desktop app (egui) for Windows and Linux |
| `ios` | the iPad app (Apple-only; checked without linking in CI) |
| `plugins` | the bundled `.prostudio-plugin` files |
| `packaging` | Windows installer / Linux packages |
| `docs` | [plugins.md](docs/plugins.md), images, icons |

---

## The rules that keep the build green

1. **CI is the only compiler for Android/Kotlin.** Do not run `./gradlew` or `cargo ndk`
   locally in constrained environments. Push small commits; CI (GitHub Actions, `ci.yml`)
   is the verifier. Android jobs need `getValue`/`setValue` retained for delegated
   properties — sweeping unused imports is required (they fail `-D warnings`) but those
   two must survive.
2. **Read failures honestly.** `gh run view <id> --log` is usually 403; instead query the
   job's check-run **annotations** (`gh api repos/artistso/Prostudio/check-runs/<job-id>/annotations
   --jq '.[] | select(.annotation_level=="failure") | .message'`). Never guess at a fix
   for a red job you cannot read.
3. **Small, reviewable pushes.** One idea per commit; conventional messages
   (`feat:`, `fix:`, `chore:`, `docs:`). Revert temporary CI instrumentation before pushing
   the real fix.
4. **Device proof before claims.** Contract §12: no APK may be called the Glass Horizon
   milestone (or "done") until the owner installs it and approves screenshots — clean
   launch, Tools expanded, Frames expanded with the board, and one frosted overlay.
5. **Preserve the engine.** Non-visual changes inside `crates/engine`, `crates/gpu` or
   `crates/core` need a clear reason and a full CI pass; the Android UI rework is not a
   licence to refactor the engine.
6. **Branch hygiene.** Do all work on the branch assigned to you (currently
   `arena/61defddd-prostudio`), push only to it, and open/keep the pull request from it.
   Never merge your own pull request; the owner merges.
7. **Attribution and licence.** MIT OR Apache-2.0, at the recipient's option. Third-party
   assets are listed in [NOTICE](NOTICE) and [ATTRIBUTION.md](ATTRIBUTION.md); keep them
   intact, and add new ones when you add assets.

---

## Practical commands

- `cargo test --workspace` — the engine's and renderer's tests (GPU tests self-skip
  without a GPU).
- `cargo fmt --all` / `cargo clippy --workspace --all-targets` — before pushing Rust.
- CI runs on pull requests and on demand (`workflow_dispatch`); the branch's pull request
  re-verifies every push to it. The Android job uploads a `prostudio-release-apk` artifact.
- Semantic commits; keep the tree buildable at every commit.

## Where to start reading

1. This file, then [README.md](README.md) (the front page) and
   [docs/plugins.md](docs/plugins.md) if you touch plugins.
2. For the Glass Horizon work: `android/.../ui/Theme.kt` (`object Glass` — the palette),
   `GlassHorizon.kt`, `GlassHorizonBoard.kt`, `GlassNodes.kt`, `GlassIcons.kt`,
   `AppRoot.kt`, and the inkframesv5 contract linked above.
3. For the engine: `crates/core/src/lib.rs` (the session type), `crates/engine/src/`.

If something in this file contradicts what you observe (renamed paths, new jobs in
`ci.yml`), trust the tree and update this file in the same commit.
