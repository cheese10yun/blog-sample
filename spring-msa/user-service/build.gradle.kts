plugins {
    kotlin("plugin.jpa")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
//    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap")
//    implementation("org.springframework.cloud:spring-cloud-starter-bus-kafka")
//    implementation("org.springframework.cloud:spring-cloud-config-monitor")
//    implementation("org.springframework.cloud:spring-cloud-starter-netflix-ribbon:2.2.9.RELEASE")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    runtimeOnly("com.h2database:h2")
}