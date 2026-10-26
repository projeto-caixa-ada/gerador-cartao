package org.solicitacao.entity;

import org.junit.jupiter.api.Test;
import org.solicitacao.enums.StatusSolicitacao;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class SolicitacaoTest {

    @Test
    void expoeTodosOsCampos() {
        Solicitacao solicitacao = new Solicitacao();
        Instant criadoEm = Instant.parse("2026-10-26T10:00:00Z");

        solicitacao.setId(1L);
        solicitacao.setClienteId("cliente");
        solicitacao.setStatus(StatusSolicitacao.CANCELADA);
        solicitacao.setCriadoEm(criadoEm);

        assertEquals(1L, solicitacao.getId());
        assertEquals("cliente", solicitacao.getClienteId());
        assertEquals(StatusSolicitacao.CANCELADA, solicitacao.getStatus());
        assertEquals(criadoEm, solicitacao.getCriadoEm());
    }

    @Test
    void onCreatePreencheCriadoEmQuandoAusente() {
        Solicitacao solicitacao = new Solicitacao("cliente", StatusSolicitacao.ABERTA);

        solicitacao.onCreate();

        assertNotNull(solicitacao.getCriadoEm());
    }

    @Test
    void onCreatePreservaCriadoEmJaExistente() {
        Solicitacao solicitacao = new Solicitacao("cliente", StatusSolicitacao.ABERTA);
        Instant criadoEm = Instant.parse("2026-10-26T10:00:00Z");
        solicitacao.setCriadoEm(criadoEm);

        solicitacao.onCreate();

        assertSame(criadoEm, solicitacao.getCriadoEm());
    }
}
