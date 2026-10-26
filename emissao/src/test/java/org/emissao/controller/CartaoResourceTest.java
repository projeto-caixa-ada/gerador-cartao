package org.emissao.controller;

import jakarta.ws.rs.core.Response;
import org.emissao.dto.CartaoEmitidoEvent;
import org.emissao.service.CartaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartaoResourceTest {

    @Mock
    CartaoService cartaoService;

    CartaoResource resource;

    @BeforeEach
    void setUp() {
        resource = new CartaoResource();
        resource.cartaoService = cartaoService;
    }

    @Test
    void buscarCartaoPorIdDelegaParaOServico() {
        CartaoEmitidoEvent evento = evento();
        when(cartaoService.buscarPorId(5001L)).thenReturn(evento);

        assertSame(evento, resource.buscarCartaoPorId(5001L));
        verify(cartaoService).buscarPorId(5001L);
    }

    @Test
    void buscarCartaoPorSolicitacaoIdDelegaParaOServico() {
        CartaoEmitidoEvent evento = evento();
        when(cartaoService.buscarPorSolicitacaoId("101")).thenReturn(evento);

        assertSame(evento, resource.buscarCartaoPorSolicitacaoId("101"));
        verify(cartaoService).buscarPorSolicitacaoId("101");
    }

    @Test
    void invalidarCacheRetornaNoContent() {
        Response response = resource.invalidarCachePorCartaoId(5001L);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(cartaoService).invalidar(5001L);
    }

    private CartaoEmitidoEvent evento() {
        return new CartaoEmitidoEvent(
                "101",
                "cliente",
                "5001",
                "1000000000000000",
                "EMITIDO",
                Instant.parse("2026-10-26T10:00:00Z"),
                Instant.parse("2026-10-26T10:00:05Z")
        );
    }
}
