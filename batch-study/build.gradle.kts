import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt") apply false
    kotlin("plugin.spring") apply false
    kotlin("plugin.jpa") apply false
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}


group = "com.batch"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}


allprojects {
    repositories {
        mavenCentral()
        jcenter()
    }
}


//allOpen {
//    annotation("javax.persistence.Entity")
//    annotation("javax.persistence.MappedSuperclass")
//    annotation("javax.persistence.Embeddable")
//}
subprojects {

    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-spring")
    apply(plugin = "maven")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

//    plugins {
//        id("org.springframework.boot") version "2.4.1"
//        id("io.spring.dependency-management") version "1.0.10.RELEASE"
//
//        kotlin("jvm") version "1.4.21"
//        kotlin("plugin.spring") version "1.4.21"
//        kotlin("plugin.jpa") version "1.4.21"
//        kotlin("kapt") version "1.4.21"
//    }

    dependencies {
//        implementation("org.springframework.boot:spring-boot-starter-batch")
//        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")



//        implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
//        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")

//        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
//        implementation("io.projectreactor:reactor-core")

//        implementation("com.querydsl:querydsl-jpa")

//        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
//        annotationProcessor(group = "com.querydsl", name = "querydsl-apt", classifier = "jpa")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.springframework.batch:spring-batch-test")

//        kapt("com.querydsl:querydsl-apt:4.4.0:jpa")
    }

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
