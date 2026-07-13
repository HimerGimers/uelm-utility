# Uelm Utility (Forge 1.8.9)

A client-side Forge 1.8.9 mod with three features:

- **Auto Replant** – when you break fully grown wheat, the mod waits a configurable delay and then replants it using wheat seeds from your hotbar (it briefly switches to the seeds and switches back).
- **Cancel Ungrown Break** – left clicks on wheat that is not fully grown (age below 7) are canceled so you never break immature crops.
- **Auto Fishing** – while holding a fishing rod, the mod casts, detects the bite (the bobber getting yanked down), reels in, and recasts after a configurable delay.

## Config

Press **Right Shift** in game to open the config GUI. The keybind is registered under the **Uelm Utility** category in `Options > Controls`, so you can rebind it there like any vanilla key.

In the GUI you can:
- Toggle each of the three features individually.
- Switch the replant delay unit between **Ticks** and **Milliseconds**.
- Set the replant delay value and the fishing recast delay (ticks).

Settings persist in `config/uelmutility.cfg` and can also be edited there directly.

## Building the Mod

**See [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md) for detailed steps.** 

**Quickest way:** Push this repo to GitHub and use GitHub Actions (automatic builds, no local setup needed).

**Local build:** Requires Java 8 JDK. Run `./gradlew build` and get the jar from `build/libs/`.

## Fair warning

Automation like auto-replant and auto-fishing counts as a disallowed macro/cheat on many multiplayer servers (Hypixel included) and can get accounts banned. Use it in singleplayer or on servers where you know it's allowed.
