# ---- Build stage: compile the jar with the Maven wrapper ----
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw && ./mvnw -q dependency:go-offline
COPY src src
RUN ./mvnw -q package -DskipTests

# ---- Run stage: small JRE image with just the jar ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/artisanmarketplace-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
# Keep the JVM inside the free-tier memory limit (512 MB)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k"
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
