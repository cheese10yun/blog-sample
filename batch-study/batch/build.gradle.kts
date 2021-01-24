plugins {
    kotlin("plugin.spring")
}

subprojects {
    dependencies{
        implementation("org.springframework.boot:spring-boot-starter-batch")
        implementation("org.springframework.boot:spring-boot-starter-validation")

        api(project(":payment:payment-domain"))
        api(project(":batch:batch-core"))


        runtimeOnly("mysql:mysql-connector-java")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.springframework.batch:spring-batch-test")
    }
}