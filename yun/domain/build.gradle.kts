plugins {
    kotlin("plugin.jpa")
    kotlin("kapt")
}


dependencies {
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("com.querydsl:querydsl-jpa")

    runtimeOnly("com.h2database:h2")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("com.querydsl:querydsl-apt:jpa")
    kapt("com.querydsl:querydsl-apt:4.2.1:jpa")
}