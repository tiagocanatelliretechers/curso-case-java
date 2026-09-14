package com.thrive.portal.api;

import com.thrive.portal.domain.Pedido;
import com.thrive.portal.domain.Usuario;
import com.thrive.portal.service.PedidoService;
import com.thrive.portal.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoApiController {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;

    public PedidoApiController(PedidoService pedidoService, UsuarioService usuarioService) {
        this.pedidoService = pedidoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<Pedido> meus(Principal principal) {
        if (principal == null) {
            return List.of();
        }
        Usuario u = usuarioService.porEmail(principal.getName()).orElse(null);
        return u == null ? List.of() : pedidoService.doCliente(u.getClienteId());
    }

    /**
     * A01 - object-level authorization ausente (IDOR na API).
     * Qualquer token valido consulta qualquer pedido pelo id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> porId(@PathVariable Long id) {
        Pedido pedido = pedidoService.porId(id);
        if (pedido == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pedido);
    }
}
