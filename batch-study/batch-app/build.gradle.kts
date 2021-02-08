plugins {
    kotlin("kapt")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("java")
}

subprojects {
    dependencies {
        api(project(":batch-support:batch-support"))
        api(project(":batch-support:batch-test"))

        implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
        implementation("io.projectreactor:reactor-core")
        
        implementation("org.jetbrains.exposed:exposed-core:${properties["exposed_version"]}")
        implementation("org.jetbrains.exposed:exposed-dao:${properties["exposed_version"]}")
        implementation("org.jetbrains.exposed:exposed-jdbc:${properties["exposed_version"]}")
        implementation("org.jetbrains.exposed:exposed-java-time:${properties["exposed_version"]}")

        runtimeOnly("mysql:mysql-connector-java")
    }

    tasks.bootJar {
        enabled = true
    }

    tasks.jar {
        enabled = false
    }
}
