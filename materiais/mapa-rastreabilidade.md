# Mapa de Rastreabilidade — Curso CASE Java

Cruza os **10 módulos** do conteúdo programático com a **aula**, o **laboratório** correspondente
e a **vulnerabilidade OWASP Top 10 (2021)** associada. Serve como material de revisão para o aluno
e como evidência de cobertura curricular para o instrutor.

| Módulo CASE | Aula | Bloco / Lab | OWASP Top 10 associado | Correção na aplicação |
|-------------|------|-------------|------------------------|-----------------------|
| 1. Understanding Application Security, Threats & Attacks | 1 | Blocos 1–2; Lab 1.1, 1.2 | Panorama do Top 10 (todas) | Reconhecimento (sem correção) |
| 2. Security Requirements Gathering | 1 | Bloco 3; Lab 1.3 | A04 Insecure Design | Requisitos derivados (abuse cases, ASVS) |
| 3. Secure Application Design & Architecture | 2 | Blocos 1–2; Lab 2.1 | A04 Insecure Design; A10 SSRF | Threat modeling STRIDE; design de mitigações |
| 4. Secure Coding — Input Validation | 2 | Blocos 3–4; Lab 2.2–2.4 | A03 Injection; A04 | SQLi parametrizado; Bean Validation; upload seguro |
| 5. Secure Coding — Authentication & Authorization | 3 | Blocos 1–4; Lab 3.1–3.5 | A01 Broken Access Control; A07 Auth Failures | IDOR corrigido; BCrypt; rate limiting; `/admin` protegido |
| 6. Secure Coding — Cryptography | 4 | Blocos 1–2; Lab 4.1–4.2 | A02 Cryptographic Failures | AES-256-GCM; JWT com verificação de assinatura |
| 7. Secure Coding — Session Management | 4 | Blocos 3–4; Lab 4.3–4.4 | A07; Session Mgmt (CWE-384/614/352) | Cookie seguro; regeneração de sessão; CSRF |
| 8. Secure Coding — Error Handling | 5 | Blocos 1–2; Lab 5.1 | A05; A09 Logging Failures | Handler global; sem stack trace; log seguro |
| 9. Static & Dynamic Testing (SAST & DAST) | 5 | Blocos 3–6; Lab 5.2–5.3 | Transversal (detecção) | SonarQube/Semgrep + OWASP ZAP (ou Fortify/AppScan/WebInspect) |
| 10. Secure Deployment & Maintenance | 6 | Blocos 1–3; Lab 6.1–6.4 | A05; A06; A08 | Actuator/H2; Dependency-Check; Docker hardened; CI gates |

## Cobertura OWASP Top 10 (2021) × Aula

| OWASP 2021 | Onde é tratado |
|------------|----------------|
| A01 Broken Access Control | Aula 3 (IDOR, `/admin`); Aula 2 (design) |
| A02 Cryptographic Failures | Aula 3 (senha); Aula 4 (AES, JWT) |
| A03 Injection | Aula 2 (SQLi, upload) |
| A04 Insecure Design | Aulas 1–2 (requisitos, threat modeling) |
| A05 Security Misconfiguration | Aulas 5–6 (erros, Actuator, Docker) |
| A06 Vulnerable Components | Aula 6 (Dependency-Check) |
| A07 Identification & Auth Failures | Aulas 3–4 (rate limit, enumeration, JWT) |
| A08 Software/Data Integrity | Aula 6 (SCA, SBOM, CI/CD) |
| A09 Logging & Monitoring Failures | Aula 5 (logging seguro, log injection) |
| A10 SSRF | Aula 2 (design/allowlist); discutido na Aula 5 |

## Convenção de branches

| Tag/branch | Estado |
|------------|--------|
| `aula-1-baseline` | App vulnerável (ponto de partida) |
| `aula-2-baseline` … `aula-6-baseline` | Estado corrigido acumulado até a aula anterior |
| `aula-N-hardened` | Solução de referência de cada aula (instrutor) |
