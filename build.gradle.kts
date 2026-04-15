plugins {
    java
    id("org.springframework.boot") version "4.0.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.4.0"
}

group = "dev.priyanshu"
version = "0.0.1-SNAPSHOT"
description = "test-driven-security"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

spotless {
    java {
        // Use the default importOrder configuration
        importOrder()

        removeUnusedImports()
        forbidWildcardImports() // or expandWildcardImports, see below
        forbidModuleImports()

        // Cleanthat will refactor your code, but it may break your style: apply it before your formatter
        cleanthat()          // has its own section below

        // Choose one of these formatters.
        googleJavaFormat()   // has its own section below

        tableTestFormatter() // has its own section below

        formatAnnotations()  // fixes formatting of type annotations, see below
    }
}
