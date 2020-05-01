dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    api(project(":domain"))

    runtimeOnly("com.h2database:h2")
}