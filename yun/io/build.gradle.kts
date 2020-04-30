dependencies {
    val fuelVersion = "2.2.1"
    val mockServerVersion = "5.7.0"

    api("com.github.kittinunf.fuel:fuel:$fuelVersion")
    api("com.github.kittinunf.fuel:fuel-jackson:$fuelVersion")

    api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")

    api("org.mock-server:mockserver-netty:$mockServerVersion")
    api("org.mock-server:mockserver-client-java:$mockServerVersion")
}