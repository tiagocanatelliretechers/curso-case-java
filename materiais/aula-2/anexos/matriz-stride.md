# Matriz de Threat Modeling (STRIDE) — Portal de Pedidos B2B

Aluno: ________________________  Data: ____/____/____

**STRIDE:** Spoofing · Tampering · Repudiation · Information Disclosure · Denial of Service · Elevation of Privilege.
**Risco:** Alto / Médio / Baixo (≈ Probabilidade × Impacto).

As linhas 1–5 são exemplos preenchidos pelo instrutor. **No Lab 2.1, adicione ≥ 3 ameaças novas** (linhas 6+).

| # | Fluxo/Componente | Categoria STRIDE | Ameaça | Risco | Mitigação proposta |
|---|------------------|------------------|--------|-------|--------------------|
| 1 | Login (`/login`, `/api/auth/login`) | Spoofing | Brute force de credenciais sem rate limiting | Alto | Throttling/lockout progressivo, MFA, senhas fortes |
| 2 | Busca de produto (`/produtos/buscar`) | Tampering / Info Disclosure | SQL Injection extrai/edita dados | Alto | Query parametrizada + validação de entrada |
| 3 | Ver pedido (`/pedidos/{id}`) | Elevation / Info Disclosure | IDOR: acessar pedido de outro cliente | Alto | Checar posse no Service + @PostAuthorize |
| 4 | Importar catálogo (`/importar-catalogo`) | Info Disclosure | SSRF a serviços internos/metadados | Alto | Allowlist de host/esquema; bloquear IPs internos |
| 5 | Upload de comprovante | Tampering / DoS | Arquivo executável ou gigante | Alto | Allowlist de tipo (magic number), limite de tamanho, rename |
| 6 | | | | | |
| 7 | | | | | |
| 8 | | | | | |

### Dica para novas ameaças (linhas 6+)
Considere também: painel `/admin` (Elevation), cookie de sessão sem flags (Info Disclosure),
`/actuator/env` (Info Disclosure), ausência de logs (Repudiation), JWT sem verificação (Spoofing/Elevation),
storage de anexos servido publicamente (Info Disclosure).
