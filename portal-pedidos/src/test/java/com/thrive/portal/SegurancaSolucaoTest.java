package com.thrive.portal;

import com.thrive.portal.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes que provam as correcoes do curso (branch solucao-hardened).
 */
@SpringBootTest
@AutoConfigureMockMvc
class SegurancaSolucaoTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProdutoService produtoService;

    // ---- Lab 2.2: SQL Injection ----
    @Test
    void buscaNormalRetornaProduto() {
        assertFalse(produtoService.buscar("Monitor").isEmpty());
    }

    @Test
    void tautologiaNaoRetornaTudo() {
        assertTrue(produtoService.buscar("' OR '1'='1").isEmpty());
    }

    @Test
    void unionNaoVazaUsuarios() {
        assertTrue(produtoService.buscar("zzz' UNION SELECT id,email,senha,0,0 FROM usuario --").isEmpty());
    }

    // ---- Lab 3.2: IDOR ----
    @Test
    @WithUserDetails("joao@acme.com")
    void naoAcessaPedidoDeOutroCliente() throws Exception {
        mvc.perform(get("/api/pedidos/2")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("joao@acme.com")
    void acessaProprioPedido() throws Exception {
        mvc.perform(get("/api/pedidos/1")).andExpect(status().isOk());
    }

    // ---- Lab 3.5: /admin exige ROLE_ADMIN ----
    @Test
    @WithMockUser(roles = "USER")
    void clienteComumNaoAcessaAdmin() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminAcessaPainel() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isOk());
    }

    // ---- Lab 4.4: CSRF ----
    @Test
    @WithUserDetails("joao@acme.com")
    void postSemCsrfEhBloqueado() throws Exception {
        mvc.perform(post("/pedidos").param("produtoId", "1").param("quantidade", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("joao@acme.com")
    void postComCsrfFunciona() throws Exception {
        mvc.perform(post("/pedidos").param("produtoId", "1").param("quantidade", "1").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
}
