plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("signing")
}

repositories {
    mavenLocal()
    mavenCentral()
}

group = "ru.ricardocraft"
version = "1.0-SNAPSHOT"

dependencies {
    compileOnly("org.fusesource.jansi:jansi:2.4.1")
    compileOnly("org.jline:jline:3.26.3")
    compileOnly("org.jline:jline-reader:3.26.3")
    compileOnly("org.jline:jline-terminal:3.26.3")
    compileOnly("org.slf4j:slf4j-api:2.0.13")

    implementation("com.google.code.gson:gson:2.11.0")
    implementation("io.netty:netty-codec-http:4.1.67.Final")
    implementation("com.github.oshi:oshi-core:5.8.1")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("io.sentry:sentry:8.0.0-alpha.4") {
        exclude("org.slf4j")
    }
    implementation("io.sentry:sentry-log4j2:8.0.0-alpha.4") {
        exclude("org.apache.logging.log4j")
    }

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

javafx {
    version = "21"
    modules("javafx.controls", "javafx.fxml", "javafx.swing", "javafx.web")
}

application {
    mainClass = "ru.ricardocraft.client.JavaFXApplication"
}

tasks.test {
    useJUnitPlatform()
}