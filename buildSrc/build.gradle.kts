plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("runServer") {
            id = "run-server"
            implementationClass = "RunServerPlugin"
        }
    }
}

repositories {
    mavenCentral()
}
