import dev.kikugie.stonecutter.data.tree.struct.ProjectNode

plugins {
    id("dev.kikugie.stonecutter")
    alias(libs.plugins.publishing)
}

stonecutter active "1.21.1-fabric" /* [SC] DO NOT EDIT */

tasks.named("publishMods") {
    group = "build"
}

