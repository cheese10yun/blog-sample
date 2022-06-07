# Logstash JDBC

* [ ] docker 구성 elasticsearch, logstash, kibana
* [ ] last_value, date converter,  index, document id, 
* [ ] SQL -> elasticsearch 성능 측정
* [ ] SQL, elasticsearch aggregation 성능 측정 

## Logstash JDBC pipeline

```conf
input {
    jdbc{
        jdbc_connection_string => "jdbc:mysql://192.168.0.27:3366/batch_study?useSSL=false"
        jdbc_user => "root"
        jdbc_password => ""
        jdbc_driver_class => "com.mysql.cj.jdbc.Driver"
        jdbc_driver_library => "/usr/share/logstash/lib/mysql-connector-java-8.0.27.jar"
        plugin_timezone => "local"
        schedule => "* * * * *"
        use_column_value => true
        tracking_column => "updated_at"
        tracking_column_type => "timestamp"
        clean_run => false
        charset => "UTF-8"
        jdbc_fetch_size => 100000
        statement => "SELECT * FROM payment_approve where updated_at >= :sql_last_value"
        last_run_metadata_path => "/usr/share/logstash/config/jdbc-payment-approve.yml"
        type => "payment_approve"
    }
}

filter {
    mutate {
        convert => {
            "amount" => "float"
        }
    }
    date_formatter {source => "approve_date" target => "approve_date" pattern => "yyyy-MM-dd"}
    date_formatter {source => "created_at" target => "created_at" pattern => "yyyy-MM-dd'T'HH:mm:ss.SSS"}
    date_formatter {source => "updated_at" target => "updated_at" pattern => "yyyy-MM-dd'T'HH:mm:ss.SSS"}
}

output {
    if[type]=="payment_approve"{
        elasticsearch {
            hosts => "192.168.0.10:9200"
            index => "payment-approve-%{+YYY.MM}"
            document_id => "%{[id]}"
        }
    }
}
```
* input
* filter
* output

### last_run_metadata_path 정리

### Aggregation

```json
GET payment-approve-2022.06/_search?size=2
{
  "query": {
    "bool": {
      "filter": [
        {
          "range": {
            "approve_date": {
              "gte": "2022-05-01",
              "lt": "2022-06-01"
            }
          }
        }
      ]
    }
  },
    "aggs": {
        "by_flower_name": {
            "date_histogram": {
                "field": "approve_date",
                "calendar_interval": "day",
                "format": "yyyy-MM-dd",
                "order": {
                  "_key": "asc"
                }
            },
            "aggs": {
                "totla_amount": {
                    "sum": {
                        "field": "amount"
                    }
                }
            }
        }
    }
}
```

approve date 기준으로 Aggregation 진행