import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.kotlin.plugin.spring") version "1.9.22"
}

group = "devel"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

val kotlinVersion = "1.9.22"
val springBootVersion = "3.3.0"
val springSecurityVersion = "6.2.1"
val log4jVersion = "2.20.0"
val jacksonVersion = "2.16.0"

dependencies {
    // Logging
    api("io.github.oshai:kotlin-logging-jvm:7.0.0")
    api("org.apache.logging.log4j:log4j-core:$log4jVersion")
    api("org.apache.logging.log4j:log4j-api:$log4jVersion")
    api("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    api("org.apache.logging.log4j:log4j-spring-boot:$log4jVersion")
    implementation("org.springframework.boot:spring-boot-starter-log4j2:$springBootVersion")
    runtimeOnly("com.lmax:disruptor:3.4.4")

    // Security
    implementation("org.springframework.boot:spring-boot-starter-security:$springBootVersion")
    implementation("org.springframework.security:spring-security-config:$springSecurityVersion")

    // OAuth2
    implementation("org.springframework.security:spring-security-oauth2-resource-server:$springSecurityVersion")
    implementation("org.springframework.security:spring-security-oauth2-jose:$springSecurityVersion")

    // Reactor
    implementation("org.springframework.boot:spring-boot-starter-webflux:$springBootVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.9.0")

    // Reactive web server

    implementation("io.projectreactor.netty:reactor-netty")
    implementation("org.springframework.boot:spring-boot-starter-reactor-netty")

    // MongoDB
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive:$springBootVersion")

    // Http Client
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("io.projectreactor:reactor-test:3.6.2")

    // Parsing, serializing JSON
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    // CalDav
    implementation("org.mnode.ical4j:ical4j:3.2.15")

    // Development
    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

configurations {
    all {
        exclude(group = "commons-logging", module = "commons-logging")
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaExec> {
    jvmArgs = listOf("-DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector")
}
