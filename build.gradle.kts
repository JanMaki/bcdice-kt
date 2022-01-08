import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
    kotlin("plugin.serialization") version "1.5.30"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("maven-publish")
}

group = "dev.simpletimer.bcdice_kt"
version = "alpha-1"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))
    implementation("org.jetbrains.kotlin", "kotlin-stdlib", "1.5.31")

    //Rubyのエンジン
    implementation("org.jruby", "jruby-complete", "9.3.2.0")
    //Jsonの解析
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.3.2")
    //HTTP
    implementation("com.github.kittinunf.fuel", "fuel", "2.3.1")
    //圧縮の解凍
    implementation("org.apache.commons","commons-compress","1.20")
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

