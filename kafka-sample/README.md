# Kafka

 * 출처: https://wedul.site/574 [wedul]

## Docker 

```
$ docker-compose up -d
$ docker ps

CONTAINER ID        IMAGE                    COMMAND                  CREATED              STATUS                  PORTS                                                NAMES
feab0b862340        wurstmeister/zookeeper   "/bin/sh -c '/usr/sb…"   About a minute ago   Up About a minute       22/tcp, 2888/tcp, 3888/tcp, 0.0.0.0:2181->2181/tcp   kafka-sample_zookeeper_1
bcc18aaa6551        wurstmeister/kafka       "start-kafka.sh"         About a minute ago   Up About a minute       0.0.0.0:9092->9092/tcp                               kafka-sample_kafka_1

$ docker exec -it kafka-sample_kafka_1 bash
$ cd /opt/kafka/bin
$ kafka-topics.sh --describe --topic test --zookeeper docker_zookeeper_1
$ kafka-console-consumer.sh --bootstrap-server wedul.pos:9092 --topic test
```