import io.swagger.v3.oas.models.servers.Server
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.14"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("com.epages.restdocs-api-spec") version "0.16.4"
    kotlin("jvm") version "1.8.20"
    kotlin("plugin.spring") version "1.8.20"
    kotlin("plugin.jpa") version "1.8.20"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

lateinit var asciidoctorExt: Configuration
val snippetsDir by extra { file("build/generated-snippets") }

asciidoctorj {
    asciidoctorExt = configurations.create("asciidoctorExt")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.16.4")

    runtimeOnly("com.h2database:h2")
    asciidoctorExt("org.springframework.restdocs:spring-restdocs-asciidoctor")
}

//restdocsApiSpec {
//    openapi3 {
//        // OpenAPI 3 문서의 제목 및 버전
//        title = "My API"
//        version = "v1"
//
//        // 문서가 저장될 경로 (예: build/generated-restdocs/openapi3/api-spec.yml)
//        outputDir = file("build/generated-restdocs/openapi3")
//
//        // Spring REST Docs에서 생성된 .adoc 파일들이 있는 경로
//        snippetsDir = file("build/generated-snippets")
//
//        // 선택적: 서버 정보, 태그, 라이선스 등을 설정합니다.
//        servers = listOf(Server(url = "https://api.example.com", description = "My API Server"))
//        tags = listOf(Tag(name = "users", description = "Operations about users"))
//        // ... 기타 설정 ...
//    }
//}


openapi { //2.3
    host = "localhost:8080"
    basePath = "/api"
    title = "My API"
    description = "My API description"
//    tagDescriptionsPropertiesFile = "src/docs/tag-descriptions.yaml"
    version = "1.0.0"
    format = "json"
}

openapi3 {
    title = "My API"
    description = "My API description"
//    tagDescriptionsPropertiesFile = "src/test/resources/tags.yaml"
    version = "0.1.0"
    format = "yaml"
}

postman {
    title = "My API"
    version = "0.1.0"
    baseUrl = "https://localhost:8080"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    outputs.dir(snippetsDir)
}

tasks.asciidoctor {
    dependsOn(tasks.test)
    val snippets = file("build/generated-snippets")
    configurations("asciidoctorExt")
    attributes["snippets"] = snippets
    inputs.dir(snippets)
    sources { include("**/index.adoc") }
    baseDirFollowsSourceFile()
}

tasks.bootJar {
    dependsOn(tasks.asciidoctor)
    from("${tasks.asciidoctor.get().outputDir}") {
        into("static/docs")
    }
}
