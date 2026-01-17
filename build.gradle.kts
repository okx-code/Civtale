plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.3.1"
    id("run-server")
}

group = findProperty("pluginGroup") as String? ?: "com.example"
version = findProperty("pluginVersion") as String? ?: "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // If this does not resolve, run `gradle downloadServer`
    compileOnly(files("run/HytaleServer.jar"))

    implementation("org.xerial:sqlite-jdbc:3.49.1.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release = 25
    }

    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")
    }

    build {
        dependsOn(shadowJar)
    }
}

