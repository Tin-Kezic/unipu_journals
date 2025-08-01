plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.spring.boot.starter.data.jdbc)
    //implementation(libs.spring.boot.starter.data.ldap)
    //implementation(libs.spring.boot.starter.data.rest)

    implementation(libs.spring.session.jdbc)

    runtimeOnly(libs.h2)
    //runtimeOnly(libs.postgresql)
}

tasks.test {
    useJUnitPlatform()
}