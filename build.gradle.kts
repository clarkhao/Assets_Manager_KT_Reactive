import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.0.5"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.7.22"
	kotlin("plugin.spring") version "1.7.22"
}

group = "com.clark.totoro"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux") {
		exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
	}
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	//swagger develop and ui
	implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.1.0")
	//java aws sdk v2
	implementation("software.amazon.awssdk:s3:2.20.46")
	//避免重启开发工具
	implementation("org.springframework.boot:spring-boot-devtools:3.0.5")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
	//AOP
	implementation("org.springframework:spring-aspects:6.0.8")
	//logging
	implementation("org.springframework.boot:spring-boot-starter-log4j2:3.0.5")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

springBoot {
	mainClass.set("com.clark.totoro.assets.AssetsApplicationKt")
}