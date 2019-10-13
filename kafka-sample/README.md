# Kafka


## Docker 

```docer
$ docker run -d --name kafka -p 2181:2181 -p 9092:9092 --env ADVERTISED_HOST=192.168.99.10 --env ADVERTISED_PORT=9092 spotify/kafka

```