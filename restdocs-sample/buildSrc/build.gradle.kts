import groovy.lang.Closure
import io.swagger.v3.oas.models.servers.Server

plugins {
    `kotlin-dsl` // Kotlin DSL을 사용하는 경우 필요합니다.
//    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("com.epages.restdocs-api-spec") version "0.16.4"
}

java.sourceCompatibility = JavaVersion.VERSION_11

repositories {


//    mavenLocal()
//    mavenCentral()
//    jcenter()
    mavenCentral() // 필요한 모든 저장소를 여기에 추가합니다.
    gradlePluginPortal()
}


dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    implementation("org.springframework.restdocs:spring-restdocs-mockmvc:2.0.7.RELEASE")
    implementation("org.springframework.boot:spring-boot-starter-json:2.7.14")
//    implementation("com.epages:restdocs-api-spec-openapi3-generator:0.16.4")
//    implementation("com.epages.restdocs-api-spec:0.16.4")

    implementation("com.epages:restdocs-api-spec-mockmvc:0.16.4")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-native-utils")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // 여기에 buildSrc에서 필요한 추가 의존성을 추가합니다.

    implementation("io.ktor:ktor-client-core:2.1.1") // Ktor core
    implementation("io.ktor:ktor-client-cio:2.1.1") // CIO 엔진
    implementation("io.ktor:ktor-client-logging:2.1.1")
    implementation("io.ktor:ktor-client-content-negotiation:2.1.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.1.1")
    compileOnlyApi("com.epages:restdocs-api-spec-gradle-plugin:0.10.3")
}