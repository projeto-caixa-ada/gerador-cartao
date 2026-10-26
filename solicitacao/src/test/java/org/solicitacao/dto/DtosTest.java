package org.solicitacao.dto;

import org.junit.jupiter.api.Test;
import org.solicitacao.entity.Solicitacao;
import org.solicitacao.enums.StatusSolicitacao;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DtosTest {

    @Test
    void solicitacaoResponseMapeiaEntidade() {
        Solicitacao solicitacao = new Solicitacao("cliente", StatusSolicitacao.VALIDADA);
        solicitacao.setId(1L);
        solicitacao.setCriadoEm(Instant.parse("2026-10-26T10:00:00Z"));

        SolicitacaoResponse response = SolicitacaoResponse.from(solicitacao);

        assertEquals(1L, response.id());
        assertEquals("cliente", response.clienteId());
        assertEquals("VALIDADA", response.status());
        assertEquals(solicitacao.getCriadoEm(), response.criadoEm());
    }

    @Test
    void solicitacaoCriadaEventMapeiaEntidade() {
        Solicitacao solicitacao = new Solicitacao("cliente", StatusSolicitacao.ABERTA);
        solicitacao.setId(2L);
        solicitacao.setCriadoEm(Instant.parse("2026-10-26T10:00:00Z"));

        SolicitacaoCriadaEvent evento = SolicitacaoCriadaEvent.from(solicitacao);

        assertEquals(2L, evento.solicitacaoId());
        assertEquals("cliente", evento.clienteId());
        assertEquals(solicitacao.getCriadoEm(), evento.dataSolicitacao());
    }

    @Test
    void recordsExpoemOsCamposRecebidos() {
        CriarSolicitacaoRequest request = new CriarSolicitacaoRequest("cliente");
        ErroResponse erro = new ErroResponse("falha");
        CartaoEmitidoEvent evento = new CartaoEmitidoEvent(
                "1",
                "cliente",
                "2",
                "1000000000000000",
                "EMITIDO",
                Instant.parse("2026-10-26T10:00:00Z"),
                Instant.parse("2026-10-26T10:00:05Z")
        );

        assertEquals("cliente", request.clienteId());
        assertEquals("falha", erro.erro());
        assertEquals("1", evento.solicitacaoId());
        assertEquals("cliente", evento.clienteId());
        assertEquals("2", evento.cartaoId());
        assertEquals("1000000000000000", evento.numeroCartao());
        assertEquals("EMITIDO", evento.status());
        assertEquals(Instant.parse("2026-10-26T10:00:00Z"), evento.dataSolicitacao());
        assertEquals(Instant.parse("2026-10-26T10:00:05Z"), evento.ocorridoEm());
        assertNull(new CriarSolicitacaoRequest(null).clienteId());
    }
}
