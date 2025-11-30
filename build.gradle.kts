plugins {
	id("org.jetbrains.kotlin.jvm") version "2.2.0"
	id("org.jetbrains.kotlin.plugin.spring") version "2.2.0"
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
}
repositories { mavenCentral() }
java.toolchain.languageVersion = JavaLanguageVersion.of(24)
val byteBuddyAgent: Configuration by configurations.creating
dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web:3.5.3")
	implementation("org.springframework.boot:spring-boot-starter-mail:3.5.3")
	implementation("org.springframework.boot:spring-boot-starter-security:3.5.3")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
	implementation("org.springframework.boot:spring-boot-starter-mustache:3.5.3")
	implementation("org.jsoup:jsoup:1.15.3")

	implementation("org.springframework.boot:spring-boot-starter-data-jdbc:3.5.3")
    //implementation("org.springframework.boot:spring-boot-starter-jooq:3.5.6")
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:2.2.0")
	runtimeOnly("org.postgresql:postgresql:42.7.2")
	// implementation("org.springframework.session:spring-session-jdbc:3.2.1")
	// implementation("org.springframework.boot:spring-boot-starter-data-ldap:3.5.3")
	// implementation("org.springframework.boot:spring-boot-starter-data-rest:3.5.3")

    testImplementation("com.h2database:h2:2.2.224")
	testImplementation("org.springframework.boot:spring-boot-starter-test:3.5.3")
	testImplementation("org.springframework.security:spring-security-test:6.0.3")
    testImplementation("io.mockk:mockk:1.13.13")
    byteBuddyAgent("net.bytebuddy:byte-buddy-agent:1.17.6")
}
kotlin.compilerOptions.freeCompilerArgs.add("-Xjsr305=strict")
tasks.test {
	useJUnitPlatform()
    jvmArgs("-Xshare:off", "-javaagent:${byteBuddyAgent.singleFile}")
}