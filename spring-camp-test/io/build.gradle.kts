plugins {
    id("org.springframework.boot")
    id("java-test-fixtures")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")


//    testImplementation("org.springframework.boot:spring-boot-starter-validation")
//    testImplementation("org.springframework.boot:spring-boot-starter-web")
//    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//    testImplementation("org.jetbrains.kotlin:kotlin-reflect")
}

