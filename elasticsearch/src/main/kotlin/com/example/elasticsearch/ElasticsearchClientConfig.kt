package com.example.elasticsearch

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate


@Configuration
class ElasticsearchClientConfig : AbstractElasticsearchConfiguration() {

    @Bean
    override fun elasticsearchClient(): RestHighLevelClient {
        val clientConfiguration = ClientConfiguration.builder()
            .connectedTo("124.80.103.104:9200", "124.80.103.104:9300")
            .build()
        // RestHighLevelClient를 만든다.
        return RestClients.create(clientConfiguration).rest()
    }

//    @Bean
//    fun elasticsearchTemplate(): ElasticsearchOperations =
//        ElasticsearchRestTemplate(elasticsearchClient())
}