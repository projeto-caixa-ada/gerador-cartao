package org.emissao.mapper;

import org.emissao.dto.CartaoEmitidoEvent;
import org.emissao.entity.Cartao;
import org.emissao.enums.StatusCartao;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CartaoMapperTest {

    @Test
    void retornaNuloParaCartaoNulo() {
        assertNull(CartaoMapper.toEvent(null));
    }

    @Test
    void mapeiaTodosOsCamposDoCartao() {
        Cartao cartao = new Cartao();
        cartao.setId(5001L);
        cartao.setSolicitacaoId("101");
        cartao.setClienteId("cliente");
        cartao.setNumero("1000000000000000");
        cartao.setDataSolicitacao(Instant.parse("2026-10-26T10:00:00Z"));
        cartao.setStatus(StatusCartao.EMITIDO);

        CartaoEmitidoEvent evento = CartaoMapper.toEvent(cartao);

        assertEquals("101", evento.solicitacaoId());
        assertEquals("cliente", evento.clienteId());
        assertEquals("5001", evento.cartaoId());
        assertEquals("1000000000000000", evento.numeroCartao());
        assertEquals("EMITIDO", evento.status());
        assertEquals(cartao.getDataSolicitacao(), evento.dataSolicitacao());
        assertNotNull(evento.ocorridoEm());
    }

    @Test
    void mapeiaStatusNulo() {
        Cartao cartao = new Cartao();
        cartao.setId(1L);
        cartao.setStatus(null);

        assertNull(CartaoMapper.toEvent(cartao).status());
    }
}
