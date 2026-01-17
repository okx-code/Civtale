import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI
import java.util.zip.ZipFile

open class RunServerPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register(
            "downloadServer",
            DownloadServerTask::class.java
        ) {
            group = "hytale"
        }

        val runTask = project.tasks.register(
            "runServer", 
            RunServerTask::class.java
        ) {
            group = "hytale"
        }

        project.tasks.findByName("shadowJar")?.let {
            runTask.configure {
                dependsOn(it)
            }
        }
    }
}

open class DownloadServerTask : DefaultTask() {
    @TaskAction
    fun run() {
        val cacheDir = File(
            project.layout.buildDirectory.asFile.get(),
            "hytale-downloader"
        ).apply { mkdirs() }

        val downloaderZip = File(cacheDir, "hytale-downloader.zip")

        try {
            URI.create("https://downloader.hytale.com/hytale-downloader.zip").toURL().openStream().use { input ->
                downloaderZip.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            ZipFile(downloaderZip).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    zip.getInputStream(entry).use { input ->
                        File(cacheDir, entry.name).outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }

            // I could catch this output and open the browser for you but for now I'm just going to assume you're smart enough to click the link
            val executableFile = if (System.getProperty("os.name").lowercase().contains("win")) {
                File(cacheDir, "hytale-downloader-windows-amd64.exe")
            } else {
                File(cacheDir, "hytale-downloader-windows-amd64")
            }
            val process = ProcessBuilder(executableFile.absolutePath).directory(cacheDir).start()
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { println(it) }
            }
            process.waitFor()

            val serverZip = cacheDir.listFiles().find { it.name.startsWith("2026") }
            ZipFile(serverZip).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    zip.getInputStream(entry).use { input ->
                        val targetFile = File(cacheDir, entry.name)
                        targetFile.parentFile?.mkdirs()
                        targetFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }

            val serverDir = File(cacheDir, "Server")
            val serverJar = File(serverDir, "HytaleServer.jar")
            val serverAot = File(serverDir, "HytaleServer.aot")
            val assetsZip = File(cacheDir, "Assets.zip")

            val runDir = File(project.projectDir, "run").apply { mkdirs() }

            serverJar.copyTo(File(runDir, serverJar.name), overwrite = true)
            serverAot.copyTo(File(runDir, serverAot.name), overwrite = true)
            assetsZip.copyTo(File(runDir, assetsZip.name), overwrite = true)

        } catch (e: Exception) {
            println("Error: ${e.message}")
            return // Tough shit
        }
    }
}

open class RunServerTask : DefaultTask() {
    @TaskAction
    fun run() {
        val runDir = File(project.projectDir, "run").apply { mkdirs() }
        val pluginsDir = File(runDir, "mods").apply { mkdirs() }
        val serverJar = File(runDir, "HytaleServer.jar")

        project.tasks.findByName("shadowJar")?.outputs?.files?.firstOrNull()?.let { shadowJar ->
            val targetFile = File(pluginsDir, shadowJar.name)
            shadowJar.copyTo(targetFile, overwrite = true)
            println("Plugin copied to: ${targetFile.absolutePath}")
        } ?: run {
            println("WARNING: Could not find shadowJar output")
        }

        println("Starting Hytale server...")
        println("Press Ctrl+C to stop the server")

        val debugMode = project.hasProperty("debug")
        val javaArgs = mutableListOf<String>()

        if (debugMode) {
            javaArgs.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
            println("Debug mode enabled. Connect debugger to port 5005")
        }

        javaArgs.addAll(listOf(
            "-XX:AOTCache=HytaleServer.aot",
            "-jar", serverJar.name,
            "--assets", "Assets.zip",
            "--assets", "../Assets",
            "--disable-sentry",
            "--allow-op"
        ))

        // Start the server process
        val process = ProcessBuilder("java", *javaArgs.toTypedArray())
            .directory(runDir)
            .start()

        project.gradle.buildFinished {
            if (process.isAlive) {
                println("\nStopping server...")
                process.destroy()
            }
        }

        Thread {
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { println(it) }
            }
        }.start()

        Thread {
            process.errorStream.bufferedReader().useLines { lines ->
                lines.forEach { System.err.println(it) }
            }
        }.start()

        Thread {
            System.`in`.bufferedReader().useLines { lines ->
                lines.forEach {
                    process.outputStream.write((it + "\n").toByteArray())
                    process.outputStream.flush()
                }
            }
        }.start()

        val exitCode = process.waitFor()
        println("Server exited with code $exitCode")
    }
}
