import groovy.lang.Closure
import io.swagger.v3.oas.models.servers.Server
import org.yaml.snakeyaml.Yaml
import java.io.InputStream

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

fun getSpringAppName(): String? {
    val inputStream: InputStream = File("src/main/resources/application.yml").inputStream()
    val yaml = Yaml().load<Map<String, Any>>(inputStream)

    val springMap = yaml["spring"] as Map<*, *>
    val applicationMap = springMap["application"] as Map<*, *>
    return applicationMap["name"] as String?
}
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

openapi3 {
    setServers(
        listOf(
            object : Closure<Server>(this) {
                fun doCall(server: Server) {
                    server.url = "http://localhost:8080"
                    server.description = "Sandbox server"
                }
            },
            object : Closure<Server>(this) {
                fun doCall(server: Server) {
                    server.url = "http://localhost:2222"
                    server.description = "Dev server"
                }
            }
        )
    )
    title = "Member API"
    description = "My API description"
    tagDescriptionsPropertiesFile = "src/test/resources/tags-descriptions.yaml"
    version = "0.1.0"
    format = "yaml"
    outputFileNamePrefix = "openapi-${getSpringAppName().orEmpty()}"
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