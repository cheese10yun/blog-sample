plugins {
    kotlin("kapt")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
}

subprojects {
    dependencies{
        implementation("org.springframework.boot:spring-boot-starter-batch")
        implementation("org.springframework.boot:spring-boot-starter-validation")

        runtimeOnly("mysql:mysql-connector-java")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.springframework.batch:spring-batch-test")
    }
}