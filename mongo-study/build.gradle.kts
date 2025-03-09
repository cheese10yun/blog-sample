import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

plugins {
    id("org.springframework.boot") version "2.7.14"
//    id("org.jetbrains.kotlin.plugin.spring") version "1.6.21"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    kotlin("kapt") version "1.6.21"
}

allOpen {
    // Spring Data MongoDB에서 사용되는 @Document를 열어줌
    annotation("org.springframework.data.mongodb.core.mapping.Document")
}


group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

extra["testcontainersVersion"] = "1.18.3"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
    implementation("com.flipkart.zjsonpatch:zjsonpatch:0.4.11")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

//    implementation("com.querydsl:querydsl-mongodb:5.0.0"){
//        exclude(group = "org.mongodb", module = "mongo-java-driver")
//    }
//
//
//    kapt("com.querydsl:querydsl-apt:5.0.0")

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
//    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo")
//    testImplementation("org.testcontainers:junit-jupiter")
//    testImplementation("org.testcontainers:mongodb")

//    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.4.2")
//    testImplementation("io.kotest:kotest-assertions-core:5.4.2")
//    testImplementation("io.kotest:kotest-framework-engine:5.4.2")
//    testImplementation(kotlin("test"))
    testImplementation(kotlin("test"))


    implementation("io.ktor:ktor-client-core:2.2.4")
    // 기본적으로 사용하는 클라이언트 엔진 (여기서는 CIO 엔진 예시)
    implementation("io.ktor:ktor-client-cio:2.2.4")

    // (선택 사항) JSON 직렬화 및 콘텐츠 협상 기능 사용 시
    implementation("io.ktor:ktor-client-content-negotiation:2.2.4")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.2.4")

    // (선택 사항) 디버깅이나 로깅을 위한 모듈
    implementation("io.ktor:ktor-client-logging:2.2.4")
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    }
}

kapt {
    annotationProcessor("org.springframework.data.mongodb.repository.support.MongoAnnotationProcessor")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}