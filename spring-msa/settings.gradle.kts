rootProject.name = "spring-msa"

include("eureka-server")
include("gateway-server")
include("config-server")

include("order-service")
include("cart-service")
include("user-service")
include("catalog-service")

pluginManagement {
    repositories {
        gradlePluginPortal()
    }

    val pluginVersions = mapOf(
        "org.jetbrains.kotlin" to "1.6.10",
        "org.jetbrains.kotlin.plugin" to "1.6.10",
        "org.springframework" to "2.6.3",
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