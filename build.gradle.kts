import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("plugin.serialization") version "1.9.22"
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "dev.simpletimer.bcdice_kt"
version = "1.7.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    //Rubyのエンジン
    implementation("org.jruby", "jruby-complete", "9.4.3.0")
    //Jsonの解析
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.6.0")
    //HTTP
    implementation("com.github.kittinunf.fuel", "fuel", "2.3.1")
    //圧縮の解凍
    implementation("org.apache.commons","commons-compress","1.24.0")

    //KDOCの生成
    dokkaHtmlPlugin("org.jetbrains.dokka", "kotlin-as-java-plugin", "1.6.10")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "dev.simpletimer.bcdice_kt.BCDice"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_16.toString()
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveBaseName.set("bcdice-kt")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["kotlin"])
                artifact(tasks["shadowJar"])
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

