package com.thrive.portal.controller;

import com.thrive.portal.domain.ItemPedido;
import com.thrive.portal.domain.Pedido;
import com.thrive.portal.domain.Usuario;
import com.thrive.portal.service.PedidoService;
import com.thrive.portal.service.ProdutoService;
import com.thrive.portal.service.UploadService;
import com.thrive.portal.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final ProdutoService produtoService;
    private final UploadService uploadService;
    private final UsuarioService usuarioService;

    public PedidoController(PedidoService pedidoService, ProdutoService produtoService,
                            UploadService uploadService, UsuarioService usuarioService) {
        this.pedidoService = pedidoService;
        this.produtoService = produtoService;
        this.uploadService = uploadService;
        this.usuarioService = usuarioService;
    }

    private Long clienteIdDe(Principal principal) {
        return usuarioService.porEmail(principal.getName())
                .map(Usuario::getClienteId)
                .orElse(null);
    }

    @GetMapping
    public String meus(Principal principal, Model model) {
        Long clienteId = clienteIdDe(principal);
        model.addAttribute("pedidos", clienteId == null ? List.of() : pedidoService.doCliente(clienteId));
        model.addAttribute("produtos", produtoService.listarTodos());
        return "pedidos/lista";
    }

    /**
     * A01 - IDOR: exibe qualquer pedido informado pelo {id}, sem checar se
     * pertence ao cliente autenticado. Troque o id na URL para ver o pedido
     * de outro cliente. Corrigido no Lab 3.2.
     */
    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model) {
        Pedido pedido = pedidoService.porId(id);
        model.addAttribute("pedido", pedido);
        return "pedidos/detalhe";
    }

    @PostMapping
    public String criar(@RequestParam Long produtoId,
                        @RequestParam(defaultValue = "1") Integer quantidade,
                        Principal principal, RedirectAttributes ra) {
        Long clienteId = clienteIdDe(principal);
        List<ItemPedido> itens = new ArrayList<>();
        ItemPedido item = new ItemPedido();
        item.setProdutoId(produtoId);
        item.setProdutoNome(
                produtoService.porId(produtoId) == null ? "?" : produtoService.porId(produtoId).getNome());
        item.setQuantidade(quantidade);
        itens.add(item);
        Pedido pedido = pedidoService.criar(clienteId, itens);
        ra.addFlashAttribute("mensagem", "Pedido #" + pedido.getId() + " criado.");
        return "redirect:/pedidos";
    }

    @PostMapping("/{id}/comprovante")
    public String enviarComprovante(@PathVariable Long id,
                                    @RequestParam("arquivo") MultipartFile arquivo,
                                    RedirectAttributes ra) throws Exception {
        // A04 - sem validacao de tipo/tamanho (UploadService)
        uploadService.salvar(id, arquivo);
        ra.addFlashAttribute("mensagem", "Comprovante enviado.");
        return "redirect:/pedidos/" + id;
    }
}
