rootProject.name = "batch-study"

include(
    "batch-app",
    "batch-app:batch-support",
    "batch-app:batch-test",
    "batch-app:batch-csv-reader",
    "batch-app:batch-csv-writer",
    "batch-app:batch-bulk-insert"
)

include(
    "batch-support"
)

include(
    "payment",
    "payment:payment-domain",
    "payment:payment-api"
)


pluginManagement {
    repositories {
        gradlePluginPortal()
    }

    val pluginVersions = mapOf(
        "org.jetbrains.kotlin" to "1.4.21",
        "org.jetbrains.kotlin.plugin" to "1.4.21",
        "org.springframework" to "2.4.1",
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