rootProject.name = "yun"

include("api")
include("domain")
include(
    "batch",
    "batch:batch-support",
    "batch:batch-sample"
)
include(
    "io",
    "io:io-slack",
    "io:io-spring",
    "io:io-spring:io-slack-spring"
)

include(
    "support"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
    }

    val pluginVersions = mapOf(
        "org.jetbrains.kotlin" to "1.3.71",
        "org.jetbrains.kotlin.plugin" to "1.3.71",
        "org.springframework" to "2.2.6.RELEASE",
        "io.spring" to "1.0.9.RELEASE"
    )

    resolutionStrategy {
        eachPlugin {
            if (pluginVersions.containsKey(requested.id.namespace)) {
                useVersion(pluginVersions[requested.id.namespace])
            }
        }
    }
}