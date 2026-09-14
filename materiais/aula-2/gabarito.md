# Aula 2 — Gabarito / Solução comentada (SOMENTE INSTRUTOR)

Código de referência corresponde à branch `aula-2-hardened`.

## Lab 2.1 — Matriz STRIDE (linhas novas de referência)

| # | Fluxo | STRIDE | Ameaça | Risco | Mitigação |
|---|-------|--------|--------|-------|-----------|
| 6 | `/admin` | Elevation of Privilege | Cliente comum acessa painel admin | Alto | `@PreAuthorize("hasRole('ADMIN')")` / `hasRole` no filtro |
| 7 | Cookie de sessão | Information Disclosure | Roubo de sessão via XSS (sem HttpOnly) | Médio | HttpOnly+Secure+SameSite |
| 8 | Logs | Repudiation | Ações sem trilha de auditoria | Médio | Log de eventos de segurança (sem dado sensível) |
| 9 | `/actuator/env` | Information Disclosure | Exposição de segredos | Alto | Proteger/desabilitar Actuator em prod |

## Lab 2.2 — SQL Injection corrigido

**`ProdutoService.buscar` (parametrizado):**
```java
public List<Produto> buscar(String termo) {
    String sql = "SELECT id, nome, descricao, preco, estoque FROM produto "
            + "WHERE nome LIKE ? OR descricao LIKE ?";
    String like = "%" + termo + "%";               // curinga vai no PARAMETRO
    return jdbcTemplate.query(sql, ps -> {
        ps.setString(1, like);
        ps.setString(2, like);
    }, (rs, rowNum) -> {
        Produto p = new Produto();
        p.setId(rs.getLong("id"));
        p.setNome(rs.getString("nome"));
        p.setDescricao(rs.getString("descricao"));
        p.setPreco(rs.getBigDecimal("preco"));
        p.setEstoque(rs.getObject("estoque") == null ? null : rs.getInt("estoque"));
        return p;
    });
}
```
> Alternativa JPA: `@Query("SELECT p FROM Produto p WHERE p.nome LIKE %:t% OR p.descricao LIKE %:t%")`.

**Por que funciona:** o valor de `termo` é enviado ao banco como *parâmetro*, separado do texto da
query. Aspas e `UNION` no input passam a ser tratados como dados literais, não como SQL.

**Teste JUnit (`ProdutoServiceTest`):**
```java
@SpringBootTest
class ProdutoServiceTest {
    @Autowired ProdutoService produtoService;

    @Test
    void buscaNormalRetornaProduto() {
        assertFalse(produtoService.buscar("Monitor").isEmpty());
    }

    @Test
    void sqlInjectionNaoRetornaTudo() {
        // no baseline retornava todos os produtos; corrigido, retorna vazio
        assertTrue(produtoService.buscar("' OR '1'='1").isEmpty());
    }

    @Test
    void unionNaoVazaUsuarios() {
        var r = produtoService.buscar("zzz' UNION SELECT id,email,senha,0,0 FROM usuario --");
        assertTrue(r.isEmpty());
    }
}
```
(usar `org.junit.jupiter.api.Assertions.*`)

## Lab 2.3 — Bean Validation + `@CNPJ`

**DTO com anotações:**
```java
public class CadastroClienteForm {
    @NotBlank @Size(max = 150)
    private String razaoSocial;

    @NotBlank @CNPJ
    private String cnpj;

    @NotBlank @Email @Size(max = 180)
    private String email;

    @NotBlank @Size(min = 8, max = 100)
    private String senha;
    // getters/setters
}
```

**Anotação `@CNPJ`:**
```java
@Documented
@Constraint(validatedBy = CnpjValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CNPJ {
    String message() default "CNPJ invalido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

**Validator (dígitos verificadores):**
```java
public class CnpjValidator implements ConstraintValidator<CNPJ, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null) return false;
        String cnpj = value.replaceAll("\\D", "");
        if (cnpj.length() != 14 || cnpj.chars().distinct().count() == 1) return false;
        int[] p1 = {5,4,3,2,9,8,7,6,5,4,3,2};
        int[] p2 = {6,5,4,3,2,9,8,7,6,5,4,3,2};
        return dig(cnpj,12,p1) == (cnpj.charAt(12)-'0')
            && dig(cnpj,13,p2) == (cnpj.charAt(13)-'0');
    }
    private int dig(String c, int len, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < len; i++) soma += (c.charAt(i)-'0') * pesos[i];
        int r = soma % 11;
        return (r < 2) ? 0 : 11 - r;
    }
}
```

**Controller com `@Valid`:**
```java
@PostMapping("/registrar")
public String registrar(@Valid @ModelAttribute("form") CadastroClienteForm form,
                        BindingResult br, Model model) {
    if (br.hasErrors()) {
        return "registrar";           // reexibe com mensagens; não vaza detalhes
    }
    // ... persiste ...
    return "login";
}
```
> Nota: valide também unicidade de e-mail no Service (regra de negócio), retornando mensagem
> **genérica** para não permitir user enumeration (ligação com Aula 3).

## Lab 2.4 — Upload seguro

**`UploadService.salvar` corrigido:**
```java
private static final long MAX = 5L * 1024 * 1024; // 5 MB
private static final Map<String,String> ASSINATURAS = Map.of(
    "pdf", "25504446",      // %PDF
    "png", "89504e47",      // .PNG
    "jpg", "ffd8ff"         // JPEG
);

public Comprovante salvar(Long pedidoId, MultipartFile arquivo) throws IOException {
    if (arquivo.isEmpty() || arquivo.getSize() > MAX) {
        throw new IllegalArgumentException("Arquivo ausente ou acima do limite");
    }
    byte[] head = new byte[8];
    try (var in = arquivo.getInputStream()) { in.read(head); }
    String hex = HexFormat.of().formatHex(head);
    String tipo = ASSINATURAS.entrySet().stream()
        .filter(e -> hex.startsWith(e.getValue()))
        .map(Map.Entry::getKey).findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Tipo de arquivo nao permitido"));

    Path dir = Paths.get(uploadDir);
    Files.createDirectories(dir);
    String nomeSeguro = UUID.randomUUID() + "." + tipo;   // nome gerado pelo servidor
    Path destino = dir.resolve(nomeSeguro).normalize();
    if (!destino.startsWith(dir.toAbsolutePath().normalize().toString()) 
        && !destino.startsWith(dir)) {
        throw new IllegalArgumentException("Caminho invalido");
    }
    arquivo.transferTo(destino.toAbsolutePath());

    Comprovante c = new Comprovante();
    c.setPedidoId(pedidoId);
    c.setNomeArquivo(nomeSeguro);
    c.setCaminho(destino.toString());
    c.setContentType(arquivo.getContentType());
    c.setTamanho(arquivo.getSize());
    return comprovanteRepository.save(c);
}
```
Também configurar limite no `application.yml`:
```yaml
spring.servlet.multipart.max-file-size: 5MB
spring.servlet.multipart.max-request-size: 6MB
```

**Por que funciona:** o tipo é decidido pelo **conteúdo** (magic number), o nome é gerado pelo
servidor (sem path traversal) e o tamanho é limitado (anti-DoS).

## Quiz — ver `quiz.md`.
