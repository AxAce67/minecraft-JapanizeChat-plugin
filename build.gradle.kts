plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Oldest supported API (the first with MiniMessage), so nothing newer is used by accident.
    compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")

    testImplementation("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 17 // Paper 1.18.2 runs on Java 17
    options.compilerArgs.addAll(listOf("-Xlint:all,-processing,-serial", "-Werror"))
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    runServer {
        minecraftVersion("26.3")
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
