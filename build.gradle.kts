plugins {
	id("org.jetbrains.kotlin.jvm") version "2.2.0"
	id("org.jetbrains.kotlin.plugin.spring") version "2.2.0"
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
}
repositories {
	mavenCentral()
}
java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}
dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web:3.5.3")
	implementation("org.springframework.boot:spring-boot-starter-mail:3.5.3")
	implementation("org.springframework.boot:spring-boot-starter-security:3.5.3")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
	implementation("org.jetbrains.kotlin:kotlin-reflect:2.2.0")
	implementation("org.springframework.boot:spring-boot-starter-mustache:3.5.3")
	implementation("org.jsoup:jsoup:1.15.3")

	implementation("org.springframework.boot:spring-boot-starter-data-jdbc:3.5.3")
	runtimeOnly("com.h2database:h2:2.2.224")
	// runtimeOnly("org.postgresql:postgresql:42.7.2")
	// implementation("org.springframework.session:spring-session-jdbc:3.2.1")
	// implementation("org.springframework.boot:spring-boot-starter-data-ldap:3.5.3")
	// implementation("org.springframework.boot:spring-boot-starter-data-rest:3.5.3")

	testImplementation("org.springframework.boot:spring-boot-starter-test:3.5.3")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.2.0")
	testImplementation("org.springframework.security:spring-security-test:6.0.3")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.3")
}
kotlin {
	compilerOptions {
		freeCompilerArgs.add("-Xjsr305=strict")
	}
}
tasks.test {
	useJUnitPlatform()
    jvmArgs("-Xshare:off")
}
tasks.jar {
    archiveBaseName.set("unipu_journals")
    archiveVersion.set("0.0.1")
}