plugins {
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    kotlin("kapt")
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("com.querydsl:querydsl-jpa")
    api("org.jetbrains.exposed:exposed-core:${properties["exposed_version"]}")
    api("org.jetbrains.exposed:exposed-dao:${properties["exposed_version"]}")
    api("org.jetbrains.exposed:exposed-jdbc:${properties["exposed_version"]}")
    api("org.jetbrains.exposed:exposed-java-time:${properties["exposed_version"]}")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor(group = "com.querydsl", name = "querydsl-apt", classifier = "jpa")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("mysql:mysql-connector-java")

    kapt("com.querydsl:querydsl-apt:4.3.1:jpa")
}
