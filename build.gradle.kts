import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.6.10"
}

group = "dev.simpletimer.bcdice_kt"
version = "1.3.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    //Rubyのエンジン
    implementation("org.jruby", "jruby-complete", "9.3.4.0")
    //Jsonの解析
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.3.2")
    //HTTP
    implementation("com.github.kittinunf.fuel", "fuel", "2.3.1")
    //圧縮の解凍
    implementation("org.apache.commons","commons-compress","1.20")

    //KDOCの生成
    dokkaHtmlPlugin("org.jetbrains.dokka", "kotlin-as-java-plugin", "1.6.10")
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

