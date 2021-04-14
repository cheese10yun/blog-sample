rootProject.name = "spring-gateway"

include("order-service")
include("cart-service")
include("eureka-server")
include("gateway-server")
include("config-server")

pluginManagement {
    val pluginVersions = mapOf(
        "org.jetbrains.kotlin" to "1.4.31",
        "org.jetbrains.kotlin.plugin" to "1.4.31",
        "org.springframework" to "2.4.4",
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