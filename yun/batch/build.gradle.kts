dependencies {
    implementation("org.springframework.boot:spring-boot-starter-batch")

//    runtimeOnly("mysql:mysql-connector-java")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.batch:spring-batch-test")

    runtimeOnly("com.h2database:h2")
}