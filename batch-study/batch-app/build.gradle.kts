plugins {
    kotlin("kapt")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("java")
}

subprojects {
    dependencies {
        api(project(":batch-support:batch-support"))

        implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
        implementation("io.projectreactor:reactor-core")
        implementation("org.jetbrains.exposed:exposed-core:${properties["exposed_version"]}")
        implementation("org.jetbrains.exposed:exposed-dao:${properties["exposed_version"]}")
        implementation("org.jetbrains.exposed:exposed-jdbc:${properties["exposed_version"]}")
        implementation("org.jetbrains.exposed:exposed-java-time:${properties["exposed_version"]}")
        implementation("com.github.kittinunf.fuel:fuel:2.3.1")
        runtimeOnly("mysql:mysql-connector-java")

        testApi(project(":batch-support:batch-test"))
    }

    tasks.bootJar {
        enabled = true
    }

    tasks.jar {
        enabled = false
    }
}
