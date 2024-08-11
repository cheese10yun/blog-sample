rootProject.name = "spring-camp-test"

include(
    "api",
    "domain",
    "service",
    "io"
)

pluginManagement {
    repositories {
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
        gradlePluginPortal()
    }

    val pluginVersions = mapOf(
        "org.jetbrains.kotlin" to "1.9.23",
        "org.jetbrains.kotlin.plugin" to "1.9.23",
        "org.springframework" to "3.2.4",
        "io.spring" to "1.1.4"
    )

    resolutionStrategy {
        eachPlugin {
            if (pluginVersions.containsKey(requested.id.namespace)) {
                useVersion(pluginVersions[requested.id.namespace])
            }
        }
    }
}
