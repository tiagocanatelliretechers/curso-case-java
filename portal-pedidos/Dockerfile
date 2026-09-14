# =====================================================================
# Dockerfile ENDURECIDO (Lab 6.3): multi-stage, usuario nao-root, sem segredos.
# Segredos (PORTAL_JWT_SECRET, PORTAL_CRYPTO_KEY, DB_PASSWORD) vem em RUNTIME.
# =====================================================================

# ---- build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /src
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -q -B -DskipTests dependency:go-offline
COPY src/ src/
RUN ./mvnw -q -B -DskipTests package

# ---- runtime stage ----
FROM eclipse-temurin:17-jre
RUN useradd -r -u 1001 appuser
WORKDIR /app
COPY --from=build /src/target/portal-pedidos.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
