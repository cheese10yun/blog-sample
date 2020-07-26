# Spring with docker


## Spring Code
```kotlin
@RestController
class GreetingsController {
    @GetMapping("/greetings")
    fun greetings() = "hello form docker"
}

```

## dockerfile 
```dockerfile
FROM openjdk:8-jdk-alpine

ADD /build/libs/*.jar spring-docker.jar

ENTRYPOINT ["java","-jar", "spring-docker.jar"]
```

```
$ docker build . -t docker-test
$ docker run -d -p8080:8080 docker-test
$ curl -XGET localhost:8080/greetings
hello form docker
```

## Docker Hub
```
$ docker login
$ docker build . t <your docker hub ID>/spring-docker
$ docker push <your docker hub ID>/spring-docker
```