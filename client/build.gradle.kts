plugins {
    java
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

group = "ru.ricardocraft.client"
version = "1.0.0"

dependencies {
    compileOnly("org.fusesource.jansi:jansi:2.4.1")
    compileOnly("org.jline:jline:3.26.3")
    compileOnly("org.jline:jline-reader:3.26.3")
    compileOnly("org.jline:jline-terminal:3.26.3")
    compileOnly("org.slf4j:slf4j-api:2.0.13")

    implementation("org.springframework:spring-context:6.2.0")

    implementation("com.google.code.gson:gson:2.11.0")
    implementation("io.netty:netty-codec-http:4.1.115.Final")
    implementation("com.github.oshi:oshi-core:5.8.1")
    implementation("com.google.code.gson:gson:2.10.1")

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