import buildlogic.stringyLibs
import buildlogic.getVersion

plugins {
    `java-library`
    id("buildlogic.common")
    id("buildlogic.common-java")
    id("io.papermc.paperweight.userdev")
}

paperweight {
    injectPaperRepository = false
}

repositories {
    maven {
        name = "EngineHub"
        url = uri("https://maven.enginehub.org/repo/")
        content {
            // EngineHub mirrors Yarn metadata but not all artifacts (e.g. mergedv2),
            // which breaks Paperweight's paramMappings resolution (Gradle does not
            // fall back to other repositories once a POM is found).
            excludeModule("net.fabricmc", "yarn")
        }
    }
    maven {
        name = "PaperMC"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "FabricMC"
        url = uri("https://maven.fabricmc.net/")
    }
    mavenCentral()
    afterEvaluate {
        killNonEngineHubRepositories()
    }
}

dependencies {
    "implementation"(project(":worldedit-bukkit"))
    constraints {
        "remapper"("net.fabricmc:tiny-remapper:[${stringyLibs.getVersion("minimumTinyRemapper")},)") {
            because("Need remapper to support Java 21")
        }
    }
}

tasks.named("assemble") {
    dependsOn("reobfJar")
}
