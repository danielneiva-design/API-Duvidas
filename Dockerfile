# ---------- FASE 1: a cozinha (compila a API) ----------
FROM eclipse-temurin:25-jdk AS cozinha
WORKDIR /app

# Primeiro só o Maven e a lista de compras: baixa as bibliotecas uma vez e reaproveita
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline

# Depois o código, e compila
COPY src src
RUN ./mvnw -q package -DskipTests

# ---------- FASE 2: a vitrine (só roda a API) ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

# Um usuário sem poderes de administrador, só pra rodar a API
RUN useradd --system --uid 1001 api
USER api

COPY --from=cozinha /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Duser.timezone=America/Sao_Paulo", "-jar", "app.jar"]