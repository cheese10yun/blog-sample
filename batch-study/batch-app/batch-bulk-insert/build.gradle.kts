dependencies {
    api(project(":payment:payment-domain"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("co.elastic.apm:apm-agent-api:1.24.0")
    implementation("net.logstash.logback:logstash-logback-encoder:6.3")
    implementation("co.elastic.apm:apm-agent-api:1.24.0")
}