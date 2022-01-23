rootProject.name = "batch-study"

include(
    "batch-app",
    "batch-app:batch-csv-reader",
    "batch-app:batch-test-bandwidth",
    "batch-app:batch-csv-writer",
    "batch-app:batch-bulk-insert",
    "batch-app:batch-reader-performance"
)

include(
    "batch-support",
    "batch-support:batch-test",
    "batch-support:batch-support"
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
        "org.jetbrains.kotlin" to "1.6.10",
        "org.jetbrains.kotlin.plugin" to "1.6.10",
        "org.springframework" to "2.6.2",
        "io.spring" to "1.0.11.RELEASE"
    )

    resolutionStrategy {
        eachPlugin {
            if (pluginVersions.containsKey(requested.id.namespace)) {
                useVersion(pluginVersions[requested.id.namespace])
            }
        }
    }
}