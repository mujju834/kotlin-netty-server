plugins {
    kotlin("jvm") version "1.9.10"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor core and Netty engine
    implementation("io.ktor:ktor-server-core:2.3.4")
    implementation("io.ktor:ktor-server-netty:2.3.4"
    )
    implementation("io.ktor:ktor-server-cors:2.3.4")

    // Ktor client to handle forwarding HTTP requests
    implementation("io.ktor:ktor-client-core:2.3.4")
    implementation("io.ktor:ktor-client-cio:2.3.4")  // CIO client for HTTP

    // Logback for logging (SLF4J implementation)
    implementation("ch.qos.logback:logback-classic:1.4.11")

    // dotenv-kotlin for environment variables
    implementation("io.github.cdimascio:dotenv-kotlin:6.3.1")

    // JUnit for testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
}

application {
    mainClass.set("org.example.MainKt")  // Update to match your entry point
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))  // Ensure Java 17 compatibility
    }
}

tasks.test {
    useJUnitPlatform()
}
