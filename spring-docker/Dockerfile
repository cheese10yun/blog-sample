FROM openjdk:8-jdk-alpine

ADD /build/libs/*.jar spring-docker.jar

ENTRYPOINT ["java","-jar", "spring-docker.jar"]