# O JAR e construido e testado pelo Maven antes de montar a imagem.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Seguranca: usuario nao-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copia o artefato Maven ja validado.
COPY target/na-ponta-do-lapis-*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
