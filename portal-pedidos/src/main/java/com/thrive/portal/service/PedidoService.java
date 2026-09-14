package com.thrive.portal.service;

import com.thrive.portal.domain.ItemPedido;
import com.thrive.portal.domain.Pedido;
import com.thrive.portal.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoService produtoService;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoService produtoService) {
        this.pedidoRepository = pedidoRepository;
        this.produtoService = produtoService;
    }

    public List<Pedido> doCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    /**
     * A01 - Broken Access Control (IDOR).
     *
     * Busca o pedido apenas pelo id, SEM verificar se ele pertence ao cliente
     * autenticado. Basta trocar o {id} na URL para ver o pedido de outro cliente.
     *
     * Sera corrigido no Lab 3.2 (checagem de posse no service + @PostAuthorize).
     */
    public Pedido porId(Long id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    public Pedido criar(Long clienteId, List<ItemPedido> itens) {
        Pedido pedido = new Pedido();
        pedido.setClienteId(clienteId);
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            BigDecimal preco = produtoService.precoDe(item.getProdutoId());
            item.setPrecoUnitario(preco);
            item.setPedido(pedido);
            total = total.add(preco.multiply(BigDecimal.valueOf(item.getQuantidade())));
            pedido.getItens().add(item);
        }
        pedido.setTotal(total);
        return pedidoRepository.save(pedido);
    }

    public Pedido salvar(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }
}
