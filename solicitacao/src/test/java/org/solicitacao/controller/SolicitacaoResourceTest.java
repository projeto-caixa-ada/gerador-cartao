package org.solicitacao.controller;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.solicitacao.dto.CriarSolicitacaoRequest;
import org.solicitacao.dto.SolicitacaoResponse;
import org.solicitacao.entity.Solicitacao;
import org.solicitacao.enums.StatusSolicitacao;
import org.solicitacao.exception.ValidacaoException;
import org.solicitacao.service.SolicitacaoApplicationService;

import java.net.URI;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitacaoResourceTest {

    @Mock
    SolicitacaoApplicationService solicitacaoApplicationService;

    SolicitacaoResource resource;

    @BeforeEach
    void setUp() {
        resource = new SolicitacaoResource();
        resource.solicitacaoApplicationService = solicitacaoApplicationService;
    }

    @Test
    void criarRetornaAcceptedComLocationEEntidade() {
        Solicitacao solicitacao = solicitacao(42L, "cliente", StatusSolicitacao.ABERTA);
        when(solicitacaoApplicationService.criarSolicitacao("cliente")).thenReturn(solicitacao);

        Response response = resource.criar(new CriarSolicitacaoRequest("cliente"));

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
        assertEquals(URI.create("/api/solicitacoes/42"), response.getLocation());
        SolicitacaoResponse body = assertInstanceOf(SolicitacaoResponse.class, response.getEntity());
        assertEquals(42L, body.id());
        assertEquals("cliente", body.clienteId());
        assertEquals("ABERTA", body.status());
    }

    @Test
    void criarRejeitaCorpoNulo() {
        ValidacaoException exception = assertThrows(
                ValidacaoException.class,
                () -> resource.criar(null)
        );

        assertEquals("Corpo da requisição é obrigatório", exception.getMessage());
    }

    @Test
    void buscarPorIdDelegaParaOServico() {
        Solicitacao solicitacao = solicitacao(7L, "cliente", StatusSolicitacao.EMITIDA);
        when(solicitacaoApplicationService.buscarPorId(7L)).thenReturn(solicitacao);

        SolicitacaoResponse response = resource.buscarPorId(7L);

        assertEquals(7L, response.id());
        assertEquals("cliente", response.clienteId());
        assertEquals("EMITIDA", response.status());
        verify(solicitacaoApplicationService).buscarPorId(7L);
    }

    static Solicitacao solicitacao(Long id, String clienteId, StatusSolicitacao status) {
        Solicitacao solicitacao = new Solicitacao(clienteId, status);
        solicitacao.setId(id);
        solicitacao.setCriadoEm(Instant.parse("2026-10-26T10:00:00Z"));
        return solicitacao;
    }
}
