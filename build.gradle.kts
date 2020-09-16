plugins {
    kotlin("jvm") version "1.4.0"
    kotlin("plugin.serialization") version "1.4.0"
    kotlin("kapt") version "1.4.0"
    java
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("com.github.gmazzo.buildconfig") version "2.0.2"
}

val projectVersion = "0.2.3"
version = projectVersion
group = "yyuueexxiinngg"

repositories {
    maven(url = "https://mirrors.huaweicloud.com/repository/maven")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
    gradlePluginPortal()
    jcenter()
    mavenCentral()
}

val miraiCoreVersion = "1.2.0"
val miraiConsoleVersion = "1.0-M4"
val ktorVersion = "1.4.0"
val kotlinVersion = "1.4.0"
val kotlinSerializationVersion = "1.0.0-RC"
val snakeyamlVersion = "1.26"

fun ktor(id: String, version: String = this@Build_gradle.ktorVersion) = "io.ktor:ktor-$id:$version"
fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"
fun String.runCommand(workingDir: File): String? {
    return try {
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        proc.inputStream.bufferedReader().readText().trim()
    } catch (e: java.io.IOException) {
        e.printStackTrace()
        null
    }
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("net.mamoe:mirai-core:$miraiCoreVersion")
    compileOnly("net.mamoe:mirai-console:$miraiConsoleVersion")
    compileOnly(kotlin("serialization", kotlinVersion))
    compile("org.yaml:snakeyaml:$snakeyamlVersion")

    implementation(kotlinx("serialization-cbor", kotlinSerializationVersion))
    implementation("ch.qos.logback:logback-classic:1.2.3")

    api(ktor("server-cio"))
    api(ktor("client-okhttp"))
    api(ktor("websockets"))
    api(ktor("client-websockets"))

    api(kotlin("reflect", kotlinVersion))

    val autoService = "1.0-rc7"
    kapt("com.google.auto.service", "auto-service", autoService)
    compileOnly("com.google.auto.service", "auto-service-annotations", autoService)

    testImplementation(kotlin("stdlib-jdk8"))
    testImplementation("net.mamoe:mirai-core:$miraiCoreVersion")
    testImplementation("net.mamoe:mirai-core-qqandroid:$miraiCoreVersion")
    testImplementation("net.mamoe:mirai-console:$miraiConsoleVersion")
    testImplementation("net.mamoe:mirai-console-pure:$miraiConsoleVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin.target.compilations.all {
    kotlinOptions.freeCompilerArgs += "-Xjvm-default=enable"
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    val injectVersionToPluginDesc by register("injectVersionToPluginDesc") {
        group = "cqhttp-mirai"
        doLast {
            val pluginDescFile = File(projectDir, "src/main/resources/plugin.yml")
            val lines = pluginDescFile.readLines().toMutableList()
            lines[2] = "version: \"$projectVersion\""
            pluginDescFile.writeText(lines.joinToString(separator = "\n"))
        }
    }

    buildConfig {
        val commitHash = "git rev-parse --short HEAD".runCommand(projectDir)
        buildConfigField("String", "VERSION", "\"$projectVersion\"")
        if (commitHash != null) { buildConfigField("String", "COMMIT_HASH", "\"$commitHash\"") }
    }

    shadowJar {
        dependsOn(generateBuildConfig)
        dependsOn(injectVersionToPluginDesc)
    }
}