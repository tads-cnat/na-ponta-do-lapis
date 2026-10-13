# ---------- build ----------
FROM eclipse-temurin:21-jdk-alpine AS build

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

# Copia o JAR gerado na etapa de build
COPY --from=build /workspace/target/na-ponta-do-lapis-*.jar app.jar

# Documenta que a porta 8080 está em uso
EXPOSE 8080


# Inicia a aplicação (MaxRAMPercentage faz a JVM respeitar o limite de memória do container)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]