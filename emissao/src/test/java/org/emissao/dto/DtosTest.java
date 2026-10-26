package org.emissao.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DtosTest {

    @Test
    void recordsExpoemOsCamposRecebidos() {
        SolicitacaoValidadaMessage mensagem = new SolicitacaoValidadaMessage(
                101L,
                "cliente",
                Instant.parse("2026-10-26T10:00:00Z")
        );
        CartaoEmitidoEvent evento = new CartaoEmitidoEvent(
                "101",
                "cliente",
                "5001",
                "1000000000000000",
                "EMITIDO",
                Instant.parse("2026-10-26T10:00:00Z"),
                Instant.parse("2026-10-26T10:00:05Z")
        );
        ErroResponse erro = new ErroResponse("falha");

        assertEquals(101L, mensagem.solicitacaoId());
        assertEquals("cliente", mensagem.clienteId());
        assertEquals(Instant.parse("2026-10-26T10:00:00Z"), mensagem.dataSolicitacao());

        assertEquals("101", evento.solicitacaoId());
        assertEquals("cliente", evento.clienteId());
        assertEquals("5001", evento.cartaoId());
        assertEquals("1000000000000000", evento.numeroCartao());
        assertEquals("EMITIDO", evento.status());
        assertEquals(Instant.parse("2026-10-26T10:00:00Z"), evento.dataSolicitacao());
        assertEquals(Instant.parse("2026-10-26T10:00:05Z"), evento.ocorridoEm());

        assertEquals("falha", erro.erro());
        assertNull(new SolicitacaoValidadaMessage(null, null, null).dataSolicitacao());
    }
}
