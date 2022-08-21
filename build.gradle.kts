plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"

    id("net.mamoe.mirai-console") version "2.13.0-M1"
    id("me.him188.maven-central-publish") version "1.0.0-dev-3"
}

group = "xyz.cssxsh.mirai"
version = "1.2.7"

mavenCentralPublish {
    useCentralS01()
    singleDevGithubProject("cssxsh", "mirai-administrator")
    licenseFromGitHubProject("AGPL-3.0")
    workingDir = System.getenv("PUBLICATION_TEMP")?.let { file(it).resolve(projectName) }
        ?: project.buildDir.resolve("publishing-tmp")
    publication {
        artifact(tasks.getByName("buildPlugin"))
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    compileOnly("net.mamoe:mirai-core:2.13.0-M1")
    compileOnly("net.mamoe:mirai-core-utils:2.13.0-M1")
    api("com.cronutils:cron-utils:9.2.0") {
        exclude("org.slf4j")
        exclude("org.glassfish")
        exclude("org.javassist")
    }
    implementation("io.ktor:ktor-client-okhttp:2.1.0") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.slf4j")
    }
    implementation("io.ktor:ktor-client-encoding:2.1.0") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.slf4j")
    }
    implementation("com.squareup.okhttp3:okhttp:4.10.0") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.slf4j")
    }
    compileOnly("javax.validation:validation-api:2.0.1.Final")

    testImplementation(kotlin("test"))
}

kotlin {
    explicitApi()
}

mirai {
    jvmTarget = JavaVersion.VERSION_11
}

tasks {
    test {
        useJUnitPlatform()
    }
}
