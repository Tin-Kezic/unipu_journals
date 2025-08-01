plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.spring") version "2.2.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":domain"))
}

tasks.test {
    useJUnitPlatform()
}