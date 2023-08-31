import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.asciidoctor.convert") version "1.5.8"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
//    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8") //

//    runtimeOnly("com.h2database:h2")
    implementation("mysql:mysql-connector-java")

//    implementation("org.jetbrains.exposed:exposed-core:${properties["exposed_version"]}")
//    implementation("org.jetbrains.exposed:exposed-dao:${properties["exposed_version"]}")
//    implementation("org.jetbrains.exposed:exposed-jdbc:${properties["exposed_version"]}")
    implementation("org.jetbrains.exposed:exposed-java-time:${properties["exposed_version"]}")
//    implementation("org.jetbrains.exposed:exposed-jodatime:${properties["exposed_version"]}")
    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:${properties["exposed_version"]}")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
