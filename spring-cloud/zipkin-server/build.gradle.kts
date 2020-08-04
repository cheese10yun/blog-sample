dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
    implementation("org.springframework.cloud:spring-cloud-starter-zipkin")

    implementation("io.zipkin.java:zipkin-server:2.11.7"){
        exclude("org.apache.logging.log4j:log4j-slf4j-impl")
    }
    implementation("io.zipkin.java:zipkin-autoconfigure-ui:2.11.7")
}