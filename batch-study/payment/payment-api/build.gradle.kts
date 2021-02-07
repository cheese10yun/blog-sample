plugins {
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    kotlin("kapt")
}

dependencies {
    api(project(":payment:payment-domain"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}

tasks.bootJar {
    enabled = true
}