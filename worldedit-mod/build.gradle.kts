import net.fabricmc.loom.task.RemapJarTask
import java.util.jar.Attributes
import java.util.jar.Manifest

plugins {
    base
    id("buildlogic.common")
}

open class MergeManifests : DefaultTask() {
    @InputFiles
    val inputManifests: ConfigurableFileCollection = project.objects.fileCollection()

    @OutputFile
    val outputManifest: RegularFileProperty = project.objects.fileProperty()

    companion object {
        private fun assertEqual(old: Any?, new: Any?, key: Attributes.Name): Any? {
            assert(old == new) { "$key mismatch: $old != $new" }
            return old
        }

        private fun throwException(old: Any?, new: Any?, key: Attributes.Name) {
            throw IllegalStateException("Duplicate $key: was $old, trying to add $new")
        }

        private val MERGE_LOGIC = mapOf(
            Attributes.Name.MANIFEST_VERSION to ::assertEqual,
            Attributes.Name.IMPLEMENTATION_VERSION to ::assertEqual,
            Attributes.Name.MAIN_CLASS to ::assertEqual,
            Attributes.Name("WorldEdit-Version") to ::assertEqual,
            Attributes.Name("WorldEdit-Kind") to ::assertEqual,
        )
    }

    private fun mergeAttributes(aggregate: Attributes, input: Attributes) {
        input.forEach { (key, value) ->
            aggregate.merge(key, value) { old, new ->
                val mergeLogic = MERGE_LOGIC[key] ?: ::throwException
                mergeLogic(old, new, key as Attributes.Name)
            }
        }
    }

    @TaskAction
    fun merge() {
        val manifest = Manifest()
        inputManifests.forEach { manifestFile ->
            val inputManifest = manifestFile.inputStream().use { Manifest(it) }
            mergeAttributes(manifest.mainAttributes, inputManifest.mainAttributes)
            inputManifest.entries.forEach { (key, value) ->
                val aggregate = manifest.entries.computeIfAbsent(key) { Attributes() }
                mergeAttributes(aggregate, value)
            }
        }
        outputManifest.asFile.get().outputStream().use {
            manifest.write(it)
        }
    }
}

val mergeManifests = tasks.register<MergeManifests>("mergeManifests") {
    inputManifests.from(
    )
    outputManifest.set(project.layout.buildDirectory.file("mergeManifests/MANIFEST.MF"))
}

tasks.register<Jar>("jar") {
    dependsOn(
        mergeManifests
    )
    manifest {
        from(mergeManifests.flatMap { it.outputManifest })
    }

    duplicatesStrategy = DuplicatesStrategy.FAIL
    archiveClassifier.set("dist")
}

tasks.named("assemble") {
    dependsOn("jar")
}
