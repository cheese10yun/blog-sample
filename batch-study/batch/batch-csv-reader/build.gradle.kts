
dependencies {

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    api(project(":payment:payment-domain"))
    api(project(":batch:batch-core"))
}