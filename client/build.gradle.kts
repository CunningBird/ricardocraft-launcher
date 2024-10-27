plugins {
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("java-library")
    id("maven-publish")
    id("signing")
    id("java")
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
    maven {
        url = uri("https://maven.gravitlauncher.com")
    }
    mavenCentral()
}

group = "ru.ricardocraft"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("pro.gravit.launcher:launcher-core:5.6.8")
    implementation("pro.gravit.launcher:launcher-ws-api:5.6.8")
//    implementation("pro.gravit.launcher:launcher-client-api:5.6.8")
//    implementation("pro.gravit.launcher:launcher-client-start-api:5.6.8")
//    implementation("pro.gravit.launcher:launcher-client-starter-api:5.6.8")
    implementation("pro.gravit.utils.enfs:enfs:2.0.1-SNAPSHOT")
    implementation("io.netty:netty-codec-http:4.1.67.Final")
    implementation("com.github.oshi:oshi-core:5.8.1")
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

javafx {
    version = "21"
    modules("javafx.controls", "javafx.fxml", "javafx.swing", "javafx.web")
}



defaultTasks("build")

tasks.test {
    useJUnitPlatform()
}

tasks.create<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    archiveClassifier.set("sources")
}

tasks.create<Jar>("javadocJar") {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:unchecked")
    options.compilerArgs.add("-Xlint:deprecation")
    options.setIncremental(true)
}

tasks.create<JavaExec>("runDev") {
    dependsOn(tasks.assemble)
    group = "Execution"
    description = "Run the main class with JavaExecTask"
    classpath(sourceSets.main.get().runtimeClasspath)
    jvmArgs = listOf("-Dlauncherdebug.modules=pro.gravit.launcher.gui.JavaRuntimeModule", "-Dlauncherdebug.env=DEBUG")
    mainClass = "pro.gravit.launcher.gui.runtime.debug.DebugMain"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "pro.gravit.launcher.gui.JavaRuntimeModule"
        attributes["Module-Config-Class"] = "pro.gravit.launcher.gui.config.GuiModuleConfig"
        attributes["Module-Config-Name"] = "JavaRuntime"
        attributes["Required-Modern-Java"] = "true"
    }
    archiveFileName.set("JavaRuntime_lmodule.jar")
}