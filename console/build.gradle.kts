
plugins {
    id("org.springframework.boot") version "3.5.10"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
}

val springBootVersion = "3.5.10"
val javaVersion = "21"

group = "com.github.gridlts"
version = "0.1.0"

repositories {
    mavenCentral()
    mavenLocal()
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    systemProperty("spring.config.additional-location", "file:./build/libs/config/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-autoconfigure:${springBootVersion}")
    implementation(project(":base"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

java {
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}

tasks.register<Copy>("copyConfig") {
    from("${project(":base").projectDir}") {
        include("config/**/*.yml")
        include("config/**/*.properties")
    }
    into("${buildDir}/libs")
}

tasks.build {
    finalizedBy("copyConfig")
}

tasks.bootJar {
    finalizedBy("copyConfig")
}

tasks.bootRun {
    dependsOn("copyConfig")
}
