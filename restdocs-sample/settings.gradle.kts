rootProject.name = "restdocs-sample"

pluginManagement {
    repositories {
        gradlePluginPortal()
    }

    val pluginVersions = mapOf(
        "org.jetbrains.kotlin" to "1.8.20",
        "org.jetbrains.kotlin.plugin" to "1.8.20",
        "org.springframework" to "2.7.14",
        "io.spring" to "1.0.15.RELEASE",

        "org.asciidoctor.jvm" to "3.3.2",
        "com.epages" to "0.16.4"
    )

    resolutionStrategy {
        eachPlugin {
            if (pluginVersions.containsKey(requested.id.namespace)) {
                useVersion(pluginVersions[requested.id.namespace])
            }
        }
    }
}