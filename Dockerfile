FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY api/gradlew .
COPY api/gradle ./gradle
COPY api/build.gradle .
COPY api/settings.gradle .
COPY api/src ./src

RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar -x test

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

# 정적 사이트(web/)는 이 앱이 서빙하지 않고, EC2의 Nginx가 /var/www/paceleague에서 직접 서빙한다.
# 배포 스크립트(.github/ssm-commands.json)가 이 이미지에서 /web-dist를 꺼내
# /var/www/paceleague로 동기화하는 방식으로 api/web을 한 번의 배포로 함께 갱신한다.
COPY web /web-dist

ENTRYPOINT ["java", "-jar", "app.jar"]