## Contributing

### Repo layout

- **`common/`, `fabric/`, `neoforge/`**: Minecraft **1.21.1** (shared common code + thin loader wrappers)
- **`mc-1.20.1/`**: Minecraft **1.20.1** (separate Fabric + Forge projects, shared code in `mc-1.20.1/shared/`)
- **`mc-26.1/`**: Minecraft **26.1** (separate Fabric + NeoForge projects, shared code in `mc-26.1/shared/`)

### Toolchains

- **1.20.1**: Java **17**
- **1.21.1**: Java **21**
- **26.1**: Java **25**

### Local builds

- **1.21.1 (root projects)**:
  - `./gradlew :fabric:build`
  - `./gradlew :neoforge:build`

- **1.20.1**:
  - `./gradlew -p mc-1.20.1/fabric build`
  - `./gradlew -p mc-1.20.1/forge build`

- **26.1**:
  - `./gradlew -p mc-26.1/fabric build`
  - `./gradlew -p mc-26.1/neoforge build`

### CI

GitHub Actions builds all supported version/loader combinations and uploads the jars as workflow artifacts.

