plugins {
    id("org.springframework.boot")
    id("java-test-fixtures")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-web")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
}


allOpen {
    annotation("com.spring.camp.io.AllOpen")
}