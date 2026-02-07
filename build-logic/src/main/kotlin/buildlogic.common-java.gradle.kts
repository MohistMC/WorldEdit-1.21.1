import buildlogic.stringyLibs
import buildlogic.getLibrary

plugins {
    id("eclipse")
    id("idea")
    id("checkstyle")
    id("buildlogic.common")
}

tasks
    .withType<JavaCompile>()
    .matching { it.name == "compileJava" || it.name == "compileTestJava" }
    .configureEach {
        // TODO: re-enable this-escape when ANTLR suppresses it properly
        val disabledLint = listOf(
            "processing", "path", "fallthrough", "serial", "overloads", "this-escape",
        )
        options.release.set(21)
        options.compilerArgs.addAll(listOf("-Xlint:all") + disabledLint.map { "-Xlint:-$it" })
        options.isDeprecation = true
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

configure<CheckstyleExtension> {
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    toolVersion = "10.16.0"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform {
        includeEngines("junit-jupiter", "jqwik")
    }
}

dependencies {
    "compileOnly"(stringyLibs.getLibrary("jsr305"))
    "testImplementation"(platform(stringyLibs.getLibrary("junit-bom")))
    "testImplementation"(stringyLibs.getLibrary("junit-jupiter-api"))
    "testImplementation"(stringyLibs.getLibrary("junit-jupiter-params"))
    "testImplementation"(stringyLibs.getLibrary("jqwik"))
    "testImplementation"(platform(stringyLibs.getLibrary("mockito-bom")))
    "testImplementation"(stringyLibs.getLibrary("mockito-core"))
    "testImplementation"(stringyLibs.getLibrary("mockito-junit-jupiter"))
    "testRuntimeOnly"(stringyLibs.getLibrary("junit-jupiter-engine"))
}

configure<JavaPluginExtension> {
    withSourcesJar()
}
