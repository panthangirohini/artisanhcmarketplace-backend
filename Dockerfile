FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY . .
RUN chmod +x ./mvnw && ./mvnw -q package -DskipTests
EXPOSE 8080
CMD ["java", "-jar", "target/artisanmarketplace-0.0.1-SNAPSHOT.jar"]
