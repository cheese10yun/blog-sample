import com.example.restdocssample.plugin.OpenApiCustomExtension
import groovy.lang.Closure
import io.swagger.v3.oas.models.servers.Server
import kotlin.collections.set

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.asciidoctor.jvm.convert")
    id("com.epages.restdocs-api-spec")
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
}

apply(plugin = "openapi-upload")
apply(plugin = "open.api.build.plugin")

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    maven {
        url = uri("https://maven.aliyun.com/repository/central")
    }
    mavenCentral()
    gradlePluginPortal()

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


    runtimeOnly("com.mysql:mysql-connector-j")

    implementation("io.ktor:ktor-client-core:2.1.1") // Ktor core
    implementation("io.ktor:ktor-client-cio:2.1.1") // CIO 엔진
    implementation("io.ktor:ktor-client-logging:2.1.1")
    implementation("io.ktor:ktor-client-content-negotiation:2.1.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.1.1")

    testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:0.6.8")

//    runtimeOnly("com.h2database:h2")
    asciidoctorExt("org.springframework.restdocs:spring-restdocs-asciidoctor")

    implementation("org.apache.httpcomponents:httpclient:4.5.13")
}

openapi3 {
    val openApiExtension = extensions.getByType(OpenApiCustomExtension::class.java)
    setServers(
        listOf(
            object : Closure<Server>(this) {
                fun doCall(server: Server) {
                    server.url = openApiExtension.serverUrl
                    server.description = openApiExtension.serverDescription
                }
            },
        )
    )
    title = openApiExtension.title
    description = "My API description"
    version = openApiExtension.version
    tagDescriptionsPropertiesFile = "src/test/resources/tags-descriptions.yaml"
    format = openApiExtension.format
    outputFileNamePrefix = openApiExtension.outputFileNamePrefix
}

//postman {
//    title = "My API"
//    version = "0.1.0"
//    baseUrl = "https://localhost:8080"
//}

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


