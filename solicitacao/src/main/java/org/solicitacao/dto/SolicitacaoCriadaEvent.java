package org.solicitacao.dto;

import org.solicitacao.entity.Solicitacao;

import java.time.Instant;

public record SolicitacaoCriadaEvent(
        Long solicitacaoId,
        String clienteId,
        Instant dataSolicitacao
) {

    public static SolicitacaoCriadaEvent from(Solicitacao solicitacao) {
        return new SolicitacaoCriadaEvent(
                solicitacao.getId(),
                solicitacao.getClienteId(),
                solicitacao.getCriadoEm()
        );
    }
}
