plugins {
    kotlin("jvm")
    kotlin("plugin.spring") version "2.2.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":domain"))
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-ldap")
    implementation("org.springframework.boot:spring-boot-starter-data-rest")
    implementation("org.springframework.session:spring-session-jdbc")
    runtimeOnly("com.h2database:h2")
    //runtimeOnly("org.postgresql:postgresql")
}

tasks.test {
    useJUnitPlatform()
}