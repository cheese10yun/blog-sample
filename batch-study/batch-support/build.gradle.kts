plugins {
    kotlin("kapt")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("java")
}

subprojects {
    dependencies {
        api("org.springframework.boot:spring-boot-starter-batch")
        api("org.springframework.boot:spring-boot-starter-data-jpa")
        api("com.querydsl:querydsl-jpa")
        api("org.springframework.boot:spring-boot-starter-validation")

//        implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
//        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
//        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
//        implementation("io.projectreactor:reactor-core")

//        implementation("org.jetbrains.exposed:exposed-core:${properties["exposed_version"]}")
//        implementation("org.jetbrains.exposed:exposed-dao:${properties["exposed_version"]}")
//        implementation("org.jetbrains.exposed:exposed-jdbc:${properties["exposed_version"]}")
//        implementation("org.jetbrains.exposed:exposed-java-time:${properties["exposed_version"]}")

        runtimeOnly("mysql:mysql-connector-java")
    }

    tasks.bootJar {
        enabled = false
    }

    tasks.jar {
        enabled = true
    }
}
