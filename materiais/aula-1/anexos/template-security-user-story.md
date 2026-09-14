# Template — Abuse Cases e Security User Stories

Aluno: ________________________  Data: ____/____/____

Preencha para **2 funcionalidades** no Lab 1.3. Copie o bloco abaixo para cada uma.

---

## Funcionalidade: ______________________________________

**Fluxo funcional (1–2 linhas):**
> ________________________________________________________________

### Abuse cases
Formato: *"As an attacker, I want <ação> so that <ganho>."*

1. As an attacker, I want ______________________ so that ______________________.
2. As an attacker, I want ______________________ so that ______________________.
3. (opcional) As an attacker, I want ____________ so that ____________.

### Requisitos de segurança derivados (mínimo 3)
Cada requisito deve ser **verificável** (com critério de aceite).

| # | Requisito de segurança | Critério de aceite (teste) | OWASP / ASVS |
|---|------------------------|----------------------------|--------------|
| 1 | O sistema deve … | Dado …, quando …, então … | |
| 2 | O sistema deve … | | |
| 3 | O sistema deve … | | |

---

## Funcionalidade: ______________________________________

**Fluxo funcional (1–2 linhas):**
> ________________________________________________________________

### Abuse cases
1. As an attacker, I want ______________________ so that ______________________.
2. As an attacker, I want ______________________ so that ______________________.

### Requisitos de segurança derivados (mínimo 3)

| # | Requisito de segurança | Critério de aceite (teste) | OWASP / ASVS |
|---|------------------------|----------------------------|--------------|
| 1 | | | |
| 2 | | | |
| 3 | | | |

---

### Dica — bons requisitos de segurança são:
- **Verificáveis:** dá para escrever um teste que prova (falha/passa).
- **Específicos:** "senha via bcrypt com work factor ≥ 10", não "senha segura".
- **Rastreáveis:** ligados a uma funcionalidade e a uma ameaça (abuse case).
- **Negativos quando preciso:** "o sistema NÃO deve revelar se o e-mail existe".
