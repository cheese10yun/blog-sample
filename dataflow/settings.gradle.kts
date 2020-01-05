rootProject.name = "dataflow"

include(
        "task"
//        "task:sample-task"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
    }

    val pluginVersions = mapOf(
            "org.jetbrains.kotlin" to "1.3.61",
            "org.jetbrains.kotlin.plugin" to "1.3.61",
            "org.springframework" to "2.2.2.RELEASE",
            "io.spring" to "1.0.8.RELEASE"
    )

    resolutionStrategy {
        eachPlugin {
            if (pluginVersions.containsKey(requested.id.namespace)) {
                useVersion(pluginVersions[requested.id.namespace])
            }
        }
    }
}