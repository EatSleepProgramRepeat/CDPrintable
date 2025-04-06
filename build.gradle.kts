plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:4.0.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.0.0")
    testImplementation("org.mockito:mockito-inline:4.0.0")
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.3")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.CDPrintable.Main")
    applicationDefaultJvmArgs = listOf("-Djava.library.path=build/libs")
}