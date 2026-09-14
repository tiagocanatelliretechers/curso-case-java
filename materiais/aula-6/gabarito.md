# Aula 6 — Gabarito / Solução comentada (SOMENTE INSTRUTOR)

Corresponde à branch `aula-6-hardened`.

## Lab 6.1 — Actuator/H2 endurecidos

**`application-postgres.yml` (produção):**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: never
    env:
      show-values: never
spring:
  h2:
    console:
      enabled: false
```
**`SecurityConfig`:** proteger o que restar do Actuator:
```java
.requestMatchers("/actuator/health", "/actuator/info").permitAll()
.requestMatchers("/actuator/**").hasRole("ADMIN")
```
**Verificação:** `/actuator/env` → 401/404; `/actuator/health` → 200.

## Lab 6.2 — Dependência vulnerável

- Relatório aponta **`org.apache.commons:commons-text:1.9`** → **CVE-2022-42889 (Text4Shell)**.
- Correção no `pom.xml`:
  ```xml
  <dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-text</artifactId>
    <version>1.10.0</version>   <!-- ou superior -->
  </dependency>
  ```
- `./mvnw -Psecurity verify` deixa de reportar o CVE / não falha mais no gate de CVSS.

## Lab 6.3 — Dockerfile hardened

```dockerfile
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
USER appuser                      # nao-root
EXPOSE 8080
# segredos vem do ambiente em runtime, NAO da imagem:
ENTRYPOINT ["java","-jar","app.jar"]
```
- Sem `ENV` com segredo (passar `PORTAL_JWT_SECRET`, `PORTAL_CRYPTO_KEY`, `DB_PASSWORD` no runtime).
- `docker history` não mostra segredos; imagem final só com JRE + jar (menor superfície).
- Trivy: resolver/《aceitar com justificativa》os achados de base image; nenhum segredo embutido.

## Lab 6.4 — Pipeline CI (GitHub Actions — exemplo)

`.github/workflows/ci.yml`:
```yaml
name: ci-seguranca
on: [push, pull_request]
jobs:
  build-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: '17', cache: maven }
      - run: ./mvnw -B verify

  sast:
    runs-on: ubuntu-latest
    needs: build-test
    steps:
      - uses: actions/checkout@v4
      - uses: returntocorp/semgrep-action@v1     # ou SonarQube
        with: { config: p/java }
        # falha o job em findings de severidade alta

  sca:
    runs-on: ubuntu-latest
    needs: build-test
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: '17', cache: maven }
      - run: ./mvnw -B -Psecurity verify          # Dependency-Check (failBuildOnCVSS=7)
      - uses: actions/upload-artifact@v4
        if: always()
        with: { name: dependency-check-report, path: target/dependency-check-report.* }
```
- Reintroduzir `commons-text:1.9` → job **sca** falha (gate funcionando).
- Regra: PR não faz merge sem os 3 jobs verdes (branch protection).

## CTF — respostas de referência

- **Flag 1 (A01):** `/pedidos/2` e `/api/pedidos/2` como João → **403/404**. Posse verificada em
  `PedidoService.porIdDoCliente` (Aula 3).
- **Flag 2 (A02):** segredo **não** está em `/actuator/env` (endpoint restrito) nem no `application.yml`;
  vem de `PORTAL_JWT_SECRET`/`PORTAL_CRYPTO_KEY` (env). Dockerfile não embute segredo.
- **Flag 3 (Aula 1):** verificar o requisito do aluno; ex.: "senha via bcrypt" → atendido (Aula 3);
  "upload valida magic number" → atendido (Aula 2).
- **Flag 4 (A03):** payload `' UNION SELECT ...` retorna vazio; `ProdutoService.buscar` usa binding (Aula 2).
- **Flag 5 (A06):** `commons-text` atualizado; CVE-2022-42889 ausente no relatório (Lab 6.2).
- **Flag 6 (A09):** `/pedidos/abc` → erro genérico (handler global, Aula 5); log do import neutraliza CR/LF.

## Quiz — ver `quiz.md`.
