dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")
}

tasks.bootJar {
    enabled = true
}