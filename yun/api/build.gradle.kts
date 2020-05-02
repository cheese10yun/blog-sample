dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    api(project(":domain"))
    api(project(":service:service-order"))

    runtimeOnly("com.h2database:h2")
}