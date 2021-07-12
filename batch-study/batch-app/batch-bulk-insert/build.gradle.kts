dependencies {
    api(project(":payment:payment-domain"))
    implementation("net.logstash.logback:logstash-logback-encoder:6.3")
    implementation("co.elastic.apm:apm-agent-api:1.24.0")
}