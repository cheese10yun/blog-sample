plugins {
    `kotlin-dsl` // Kotlin DSL을 사용하는 경우 필요합니다.
}

repositories {
    mavenCentral() // 필요한 모든 저장소를 여기에 추가합니다.
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    // 여기에 buildSrc에서 필요한 추가 의존성을 추가합니다.

    implementation("io.ktor:ktor-client-core:2.1.1") // Ktor core
    implementation("io.ktor:ktor-client-cio:2.1.1") // CIO 엔진
    implementation("io.ktor:ktor-client-logging:2.1.1")
    implementation("io.ktor:ktor-client-content-negotiation:2.1.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.1.1")
}