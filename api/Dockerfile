# syntax=docker/dockerfile:1.4
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod

# 빌드된 JAR 파일을 복사
COPY build/libs/api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]