# Changelog

## 1.0.5

### HUD
- **Peripheral mode**: ARMA-style edge indicators (hug bottom + slide along left/right edges), **smaller rings**, and **green → yellow → red** threat gradient (matches chevrons).
- **Layering**:
  - **26.1**: Peripheral renders under the hotbar (Fabric + NeoForge); chevrons/meme remain unchanged.
  - **1.20.1 Forge**: Peripheral renders under the hotbar.
- **Meme mode (26.1)**: fixed tiling/UV issues and restored a smooth **alpha fade**; removed the distracting pulse.

### Fixes
- **1.21.1 NeoForge**: corrected key-mapping registration to use the proper event bus.

### CI
- **26.1 builds**: ensure the correct Gradle wrapper (`mc-26.1/gradlew`) is executable on Linux runners.

