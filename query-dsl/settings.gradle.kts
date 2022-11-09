rootProject.name = "query-dsl"


pluginManagement {
    repositories {
        gradlePluginPortal()
    }

    val pluginVersions = mapOf(
        "org.jetbrains.kotlin" to "1.6.21",
        "org.jetbrains.kotlin.plugin" to "1.6.21",
        "org.springframework" to "2.7.0",
        "io.spring" to "1.0.10.RELEASE"
    )

    resolutionStrategy {
        eachPlugin {
            if (pluginVersions.containsKey(requested.id.namespace)) {
                useVersion(pluginVersions[requested.id.namespace])
            }
        }
    }
}