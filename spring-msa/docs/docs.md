-javaagent:/Users/yun/yun/blog-sample/elk-sample/api/elastic-apm-agent-1.29.0.jar -Delastic.apm.service_name=user-service -Delastic.apm.server_url=http://192.168.0.10:8200 -Delastic.apm.application_packages=com.service.user

-javaagent:/Users/yun/yun/blog-sample/elk-sample/api/elastic-apm-agent-1.29.0.jar -Delastic.apm.service_name=order-service -Delastic.apm.server_url=http://192.168.0.10:8200 -Delastic.apm.application_packages=com.service.order

-javaagent:/Users/yun/yun/blog-sample/elk-sample/api/elastic-apm-agent-1.29.0.jar -Delastic.apm.service_name=gateway-service -Delastic.apm.server_url=http://192.168.0.10:8200 -Delastic.apm.application_packages=com.server.gateway

-javaagent:/Users/yun/yun/blog-sample/elk-sample/api/elastic-apm-agent-1.29.0.jar -Delastic.apm.service_name=config-server -Delastic.apm.server_url=http://124.80.103.104:8200 -Delastic.apm.application_packages=com.server.config

-javaagent:/Users/yun/yun/blog-sample/elk-sample/api/elastic-apm-agent-1.29.0.jar -Delastic.apm.service_name=eureka-server -Delastic.apm.server_url=http://124.80.103.104:8200 -Delastic.apm.application_packages=com.server.eureka

java -jar /Users/yun.cheese/yun/blog-sample/spring-msa/eureka-server/build/libs/eureka-server-0.0.1-SNAPSHOT.jar

java -jar /Users/yun.cheese/yun/blog-sample/spring-msa/gateway-server/build/libs/gateway-server-0.0.1-SNAPSHOT.jar