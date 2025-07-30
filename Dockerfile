FROM maven:3.9.11-ibm-semeru-17-noble AS builder
WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17.0.15_6-jdk-noble
WORKDIR /app

COPY --from=builder /build/target/jose-service-*.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
