FROM openjdk:11-jdk-slim

RUN apt-get update
RUN apt-get install -y maven

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY . .

RUN mvn package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/Broker-0.0.1-SNAPSHOT.jar"]
