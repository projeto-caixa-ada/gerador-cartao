package org.emissao.entity;

import org.emissao.enums.StatusCartao;
import org.emissao.enums.StatusEmissao;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class EntitiesTest {

    @Test
    void cartaoExpoeTodosOsCampos() {
        Cartao cartao = new Cartao();
        Instant agora = Instant.parse("2026-10-26T10:00:00Z");

        cartao.setId(5001L);
        cartao.setNumero("1000000000000000");
        cartao.setSolicitacaoId("101");
        cartao.setClienteId("cliente");
        cartao.setDataSolicitacao(agora);
        cartao.setStatus(StatusCartao.EMITIDO);
        cartao.setCriadoEm(agora);
        cartao.setAtualizadoEm(agora);

        assertEquals(5001L, cartao.getId());
        assertEquals("1000000000000000", cartao.getNumero());
        assertEquals("101", cartao.getSolicitacaoId());
        assertEquals("cliente", cartao.getClienteId());
        assertEquals(agora, cartao.getDataSolicitacao());
        assertEquals(StatusCartao.EMITIDO, cartao.getStatus());
        assertSame(agora, cartao.getCriadoEm());
        assertSame(agora, cartao.getAtualizadoEm());
    }

    @Test
    void cartaoOnCreatePreencheDatas() {
        Cartao cartao = new Cartao();

        cartao.onCreate();

        assertNotNull(cartao.getCriadoEm());
        assertNotNull(cartao.getAtualizadoEm());
    }

    @Test
    void cartaoOnUpdateAtualizaAtualizadoEm() {
        Cartao cartao = new Cartao();
        cartao.setCriadoEm(Instant.parse("2026-10-26T10:00:00Z"));
        cartao.setAtualizadoEm(Instant.parse("2026-10-26T10:00:00Z"));

        cartao.onUpdate();

        assertNotNull(cartao.getAtualizadoEm());
        assertEquals(cartao.getCriadoEm(), Instant.parse("2026-10-26T10:00:00Z"));
    }

    @Test
    void emissaoExpoeTodosOsCampos() {
        Emissao emissao = new Emissao();
        Cartao cartao = new Cartao();
        Instant agora = Instant.parse("2026-10-26T10:00:00Z");

        emissao.setId(1L);
        emissao.setSolicitacaoId("101");
        emissao.setClienteId("cliente");
        emissao.setDataSolicitacao(agora);
        emissao.setCartao(cartao);
        emissao.setStatus(StatusEmissao.EMITIDA);
        emissao.setCriadoEm(agora);
        emissao.setAtualizadoEm(agora);

        assertEquals(1L, emissao.getId());
        assertEquals("101", emissao.getSolicitacaoId());
        assertEquals("cliente", emissao.getClienteId());
        assertEquals(agora, emissao.getDataSolicitacao());
        assertSame(cartao, emissao.getCartao());
        assertEquals(StatusEmissao.EMITIDA, emissao.getStatus());
        assertSame(agora, emissao.getCriadoEm());
        assertSame(agora, emissao.getAtualizadoEm());
    }

    @Test
    void emissaoOnCreatePreencheDatas() {
        Emissao emissao = new Emissao();

        emissao.onCreate();

        assertNotNull(emissao.getCriadoEm());
        assertNotNull(emissao.getAtualizadoEm());
    }

    @Test
    void emissaoOnUpdateAtualizaAtualizadoEm() {
        Emissao emissao = new Emissao();
        Instant criadoEm = Instant.parse("2026-10-26T10:00:00Z");
        emissao.setCriadoEm(criadoEm);
        emissao.setAtualizadoEm(criadoEm);

        emissao.onUpdate();

        assertNotNull(emissao.getAtualizadoEm());
        assertSame(criadoEm, emissao.getCriadoEm());
    }
}
