FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle :backend:installDist --no-daemon --settings-file backend-settings.gradle.kts

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/build/install/backend /app
EXPOSE 8080
CMD ["/app/bin/backend"]
