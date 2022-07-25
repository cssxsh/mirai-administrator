plugins {
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"

    id("net.mamoe.mirai-console") version "2.12.0"
    id("net.mamoe.maven-central-publish") version "0.7.1"
}

group = "xyz.cssxsh.mirai"
version = "1.2.5"

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
}

dependencies {
    compileOnly("net.mamoe:mirai-core:2.12.0")
    compileOnly("net.mamoe:mirai-core-utils:2.12.0")
    api("com.cronutils:cron-utils:9.1.6") {
        exclude("org.slf4j")
        exclude("org.glassfish")
        exclude("org.javassist")
    }
    compileOnly("javax.validation:validation-api:2.0.1.Final")

    testImplementation(kotlin("test", "1.6.21"))
}

kotlin {
    explicitApi()
}

tasks {
    test {
        useJUnitPlatform()
    }
}
