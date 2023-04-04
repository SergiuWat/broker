FROM openjdk:19

COPY . /app

WORKDIR /app

EXPOSE 8080

CMD ["java", "-jar", "target/spring-boot-docker.jar"]
