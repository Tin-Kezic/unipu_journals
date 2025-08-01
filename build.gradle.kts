plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.spring)
	alias(libs.plugins.spring.boot)
	alias(libs.plugins.dependency.management)
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(24)
	}
}

dependencies {
	implementation(project(":data"))
	implementation(project(":domain"))
	implementation(project(":feature"))

	implementation(libs.spring.boot.starter.mail)
	implementation(libs.spring.boot.starter.mustache)
	// implementation(libs.spring.boot.starter.security)
	implementation(libs.jsoup)
	implementation(libs.spring.boot.starter.web)
	implementation(libs.jackson.module.kotlin)
	implementation(libs.htmx)
	implementation(libs.kotlin.reflect)

	developmentOnly(libs.spring.boot.devtools)

	testImplementation(libs.spring.boot.starter.test)
	testImplementation(libs.kotlin.test.junit5)
	testImplementation(libs.spring.security.test)
	testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.test {
	useJUnitPlatform()
}
