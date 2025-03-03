-javaagent:/Users/yun/yun/blog-sample/elk-sample/api/elastic-apm-agent-1.29.0.jar -Delastic.apm.service_name=api-sample -Delastic.apm.server_url=http://192.168.0.10:8200 -Delastic.apm.application_packages=com.example.api



-javaagent:/Users/yun/yun/blog-sample/elk-sample/api/elastic-apm-agent-1.29.0.jar -Delastic.apm.service_name=api-sample -Delastic.apm.server_url=http://192.168.0.10:8200 -Delastic.apm.application_packages=com.example.api



/Users/yun/yun/blog-sample/elk-sample/api/elastic-apm-agent-1.29.0.jar


curl -L -O https://artifacts.elastic.co/downloads/downloads/beats/elastic-agent/elastic-agent-8.17.2-amd64.deb 
sudo dpkg -i elastic-agent-8.17.2-amd64.deb 
sudo systemctl enable elastic-agent 
sudo systemctl start elastic-agent