plugins {
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.serialization") version "1.6.0"

    id("net.mamoe.mirai-console") version "2.10.1"
    id("net.mamoe.maven-central-publish") version "0.7.1"
}

group = "xyz.cssxsh.mirai"
version = "1.0.7"

mavenCentralPublish {
    useCentralS01()
    singleDevGithubProject("cssxsh", "mirai-administrator")
    licenseFromGitHubProject("AGPL-3.0", "master")
    publication {
        artifact(tasks.getByName("buildPlugin"))
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly("net.mamoe:mirai-core:2.10.1")
    compileOnly("net.mamoe:mirai-core-utils:2.10.1")

    testImplementation(kotlin("test", "1.6.0"))
}

kotlin {
    explicitApi()
}

tasks {
    test {
        useJUnitPlatform()
    }
}
