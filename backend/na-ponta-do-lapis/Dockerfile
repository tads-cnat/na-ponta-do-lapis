# Build stage
FROM maven:3.9.8-eclipse-temurin-21 AS builder

WORKDIR /app

# Cache de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compilacao do codigo fonte
COPY src ./src
RUN mvn package -DskipTests --no-transfer-progress

# Runtime stage (JRE alpine e significativamente mais leve e seguro)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Seguranca: usuario nao-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copia do JAR gerado (com wildcard para nao quebrar em mudancas de versao)
COPY --from=builder /app/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
