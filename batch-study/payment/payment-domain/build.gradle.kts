plugins {
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    kotlin("kapt")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.querydsl:querydsl-jpa")
    implementation("org.jetbrains.exposed:exposed-core:${properties["exposed_version"]}")
    implementation("org.jetbrains.exposed:exposed-dao:${properties["exposed_version"]}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${properties["exposed_version"]}")
    implementation("org.jetbrains.exposed:exposed-java-time:${properties["exposed_version"]}")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor(group = "com.querydsl", name = "querydsl-apt", classifier = "jpa")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("mysql:mysql-connector-java")

    kapt("com.querydsl:querydsl-apt:4.4.0:jpa")
}