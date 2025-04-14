/*
 * CDPrintable: A program that prints labels with track listings for your CD cases.
 * Copyright (C) 2025 Alexander McLean
 *
 * This source code is licensed under the GNU General Public License v3.0
 * found in the LICENSE file in the root directory of this source tree.
 *
 * This is the main build file.
 */

import java.util.Properties
import java.io.FileInputStream

val versionProperties = Properties()
file("src/main/resources/version.properties").takeIf { it.exists() }?.let {
    versionProperties.load(FileInputStream(it))
}

plugins {
    id("java")
    id("application")
}

group = "org.example"
version = versionProperties.getProperty("version", "0.0-VERSION-ERROR")

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.google.code.gson:gson:2.12.1")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.CDPrintable.Main")
    applicationDefaultJvmArgs = listOf("-Djava.library.path=build/libs")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(22))
    }
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
            "Main-Class" to "com.CDPrintable.Main"
        )
    }
}