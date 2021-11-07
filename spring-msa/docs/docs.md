-javaagent:/Users/yun.cheese/yun/blog-sample/spring-msa/elk/elastic-apm-agent-1.24.0.jar -Delastic.apm.service_name=user-service -Delastic.apm.server_url=http://192.168.0.10:8200 -Delastic.apm.application_packages=com.service.user

-javaagent:/Users/yun.cheese/yun/blog-sample/elk-sample/api/elastic-apm-agent-1.24.0.jar -Delastic.apm.service_name=config-server -Delastic.apm.server_url=http://124.80.103.104:8200 -Delastic.apm.application_packages=com.server.config

-javaagent:/Users/yun.cheese/yun/blog-sample/elk-sample/api/elastic-apm-agent-1.24.0.jar -Delastic.apm.service_name=eureka-server -Delastic.apm.server_url=http://124.80.103.104:8200 -Delastic.apm.application_packages=com.server.eureka

