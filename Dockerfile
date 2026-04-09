FROM gradle:8.7-jdk17 AS builder

WORKDIR /build

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x ./gradlew

COPY src ./src

RUN ./gradlew clean bootJar -x test

FROM amazoncorretto:17-alpine

WORKDIR /app
ENV TZ=Asia/Seoul

COPY --from=builder /build/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]