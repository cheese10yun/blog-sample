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
        "org.jetbrains.kotlin" to "1.6.21",
        "org.jetbrains.kotlin.plugin" to "1.6.21",
        "org.springframework" to "2.7.11",
        "io.spring" to "1.0.15.RELEASE"
    )

    resolutionStrategy {
        eachPlugin {
            if (pluginVersions.containsKey(requested.id.namespace)) {
                useVersion(pluginVersions[requested.id.namespace])
            }
        }
    }
}
