plugins {
    id("java")
}

group = "ru.ricardocraft"
version = "1.0-SNAPSHOT"

//configurations {
//    compileOnlyA
//    bundleOnly
//    bundle
//    hikari
//    pack
//    launch4j
//    bundleOnly.extendsFrom bundle
//            api.extendsFrom bundle, hikari, pack, launch4j
//}

repositories {
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
    maven {
        url = uri("https://jcenter.bintray.com/")
    }
    maven {
        url = uri("https://jitpack.io/")
    }
}

dependencies {
    implementation("me.tongfei:progressbar:0.10.1")
    implementation("org.fusesource.jansi:jansi:2.4.1")
    implementation("org.jline:jline:3.26.3")
    implementation("org.jline:jline-reader:3.26.3")
    implementation("org.jline:jline-terminal:3.26.3")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78.1")
    implementation("org.ow2.asm:asm-commons:9.7")
    implementation("io.netty:netty-codec-http:4.1.111.Final")
    implementation("io.netty:netty-transport-classes-epoll:4.1.111.Final")
    implementation("io.netty:netty-transport-native-epoll:4.1.111.Final:linux-x86_64")
    implementation("io.netty:netty-transport-native-epoll:4.1.111.Final:linux-aarch_64")
    implementation("org.slf4j:slf4j-api:2.0.13")

    implementation("com.mysql:mysql-connector-j:9.0.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.4.0")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("com.h2database:h2:2.3.232")

    implementation("com.guardsquare:proguard-base:7.5.0")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.5")
    implementation("io.jsonwebtoken:jjwt-gson:0.12.5")
    implementation("com.google.code.gson:gson:2.11.0")
    annotationProcessor("org.apache.logging.log4j:log4j-core:2.23.1")

    implementation("io.micrometer:micrometer-core:1.13.1")
    implementation("com.zaxxer:HikariCP:5.1.0") {
        exclude("javassist")
        exclude("io.micrometer")
        exclude("org.slf4j")
    }

    implementation("pro.gravit.utils.enfs:enfs:2.0.1-SNAPSHOT")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    //     testImplementation group: 'org.junit.jupiter', name: 'junit-jupiter', version: rootProject['verJunit']
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

//tasks.jar {
//    dependsOn(parent.childProjects.Launcher.tasks.assemble)
//    from { configurations.pack.collect { it.isDirectory() ? it : zipTree(it) } }
//    from(parent.childProjects.Launcher.tasks.shadowJar)
//    from(parent.childProjects.Launcher.tasks.genRuntimeJS)
//    manifest{
//        attributes["Main-Class"] = "pro.gravit.launchserver.Main"
//        attributes["Premain-Class"] = "pro.gravit.launchserver.StarterAgent"
//        attributes["Multi-Release"] = "true"
//    }
//}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

tasks.register<Jar>("cleanjar") {
    dependsOn(tasks.jar)
    archiveClassifier.set("clean")
    manifest{
        attributes["Main-Class"] = "pro.gravit.launchserver.Main"
        attributes["Automatic-Module-Name"] = "launchserver"
    }
    from(sourceSets.main.get().output)
}

//tasks.register<Copy>("hikari") {
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//    into("${layout.buildDirectory}/libs/libraries/hikaricp")
//    from(configurations.hikari)
//}
//
//tasks.register<Copy>("dumpLibs") {
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//    dependsOn(tasks.hikari)
//    into("${layout.buildDirectory}/libs/libraries")
//    from(configurations.bundleOnly)
//}
//
//tasks.register<Copy>("dumpCompileOnlyLibs") {
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//    into("${layout.buildDirectory}/libs/launcher-libraries-compile")
//    from(configurations.compileOnlyA)
//}
//
//tasks.register<Zip>("bundle") {
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//    dependsOn(parent.childProjects.Launcher.tasks.build, tasks.dumpLibs, tasks.dumpCompileOnlyLibs, tasks.jar)
//    archiveFileName.set("LaunchServer.zip")
//    destinationDirectory = file("${layout.buildDirectory}")
//    from(tasks.dumpLibs.destinationDir) { into("libraries") }
//    from(tasks.dumpCompileOnlyLibs.destinationDir) { into("launcher-libraries-compile") }
//    from(tasks.jar)
//    from(parent.childProjects.Launcher.tasks.dumpLibs) { into("launcher-libraries") }
//}
//
//tasks.register<Copy>("dumpClientLibs") {
//    dependsOn(parent.childProjects.Launcher.tasks.build)
//    into("${layout.buildDirectory}/libs/launcher-libraries")
//    from(parent.childProjects.Launcher.tasks.dumpLibs)
//}
//
//tasks.assemble {
//    dependsOn(tasks.dumpLibs, tasks.dumpCompileOnlyLibs, tasks.dumpClientLibs, tasks.bundle, tasks.cleanjar)
//}