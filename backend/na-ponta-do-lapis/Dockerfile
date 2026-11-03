# ---------- build ----------
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

# Dependências primeiro: esta camada só é refeita quando o pom.xml mudar
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B --no-transfer-progress dependency:go-offline

# Código-fonte (os testes rodam no job "test" do CI, por isso -DskipTests aqui)
COPY src src
RUN ./mvnw -B --no-transfer-progress package -DskipTests

# ---------- runtime ----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# OCR local de faturas escaneadas (o PDF não sai do servidor)
RUN apk add --no-cache tesseract-ocr tesseract-ocr-data-por

# Copia o JAR gerado na etapa de build
COPY --from=build /workspace/target/na-ponta-do-lapis-*.jar app.jar

# Documenta que a porta 8080 está em uso
EXPOSE 8080

# Healthcheck (busybox wget existe no alpine)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -qO- http://localhost:8080/api/actuator/health | grep -q UP || exit 1

# Inicia a aplicação (MaxRAMPercentage faz a JVM respeitar o limite de memória do container)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]