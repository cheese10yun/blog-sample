rootProject.name = "query-dsl"


pluginManagement {
    repositories {
        gradlePluginPortal()
    }

    val pluginVersions = mapOf(
        "org.jetbrains.kotlin" to "1.9.21",
        "org.jetbrains.kotlin.plugin" to "1.9.21",
        "org.springframework" to "3.2.1",
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