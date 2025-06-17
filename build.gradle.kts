plugins {
    kotlin("jvm") version "2.0.20"
    id("io.ktor.plugin") version "2.3.12"
}

group = "dev.limebeck"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("dev.limebeck.openconf.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)
    api(libs.hikari)
    api(libs.postgres)
    implementation("ch.qos.logback:logback-classic:1.5.8")

    implementation("dev.inmo:tgbotapi:18.2.0")
    implementation("dev.inmo:krontab:2.2.9")

    implementation("org.ktorm:ktorm-core:4.1.1")

    implementation("org.mindrot:jbcrypt:0.4")

    implementation("com.sksamuel.hoplite:hoplite-core:2.7.5")
    implementation("com.sksamuel.hoplite:hoplite-yaml:2.7.5")

    implementation("io.arrow-kt:suspendapp:0.4.0")

    implementation("com.jsoizo:kotlin-csv-jvm:1.10.0")


    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-html-builder")
    implementation("io.ktor:ktor-server-auth")

    testImplementation(kotlin("test"))

    implementation(platform("org.testcontainers:testcontainers-bom:1.21.1"))
    testImplementation("org.testcontainers:postgresql")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
    compilerOptions {
        this.freeCompilerArgs.add("-Xcontext-receivers")
    }
}