plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java-test-fixtures")
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.jetbrains.kotlin:kotlin-reflect")


    testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:1.0.23")
    testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter:1.0.23")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    runtimeOnly("com.h2database:h2")
//    runtimeOnly("com.mysql:mysql-connector-j")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testApi(testFixtures(project(":io")))

    testFixturesImplementation("org.springframework.boot:spring-boot-starter")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
}

