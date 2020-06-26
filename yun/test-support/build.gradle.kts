dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-test")
    api(project(":domain"))
    api(project(":service:service-order"))

    runtimeOnly("com.h2database:h2")

    implementation("org.testcontainers:junit-jupiter:1.14.3")
    implementation("org.testcontainers:mysql:1.14.3")
}