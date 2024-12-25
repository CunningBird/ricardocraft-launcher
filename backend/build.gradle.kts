plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "ru.ricardocraft.backend"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
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
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("io.projectreactor:reactor-core")

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
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.5")

    implementation("io.micrometer:micrometer-core:1.13.1")
    implementation("com.zaxxer:HikariCP:5.1.0") {
        exclude("javassist")
        exclude("io.micrometer")
    }

    implementation("io.sentry:sentry:8.0.0-alpha.4")

    implementation(platform("software.amazon.awssdk:bom:2.17.290"))
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:netty-nio-client")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Tools
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}