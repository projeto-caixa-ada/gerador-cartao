package org.solicitacao.dto;

import org.solicitacao.entity.Solicitacao;

import java.time.Instant;

public record SolicitacaoResponse(
        String id,
        String clienteId,
        String status,
        Instant criadoEm
) {

    public static SolicitacaoResponse from(Solicitacao solicitacao) {
        return new SolicitacaoResponse(
                solicitacao.getId(),
                solicitacao.getClienteId(),
                solicitacao.getStatus().name(),
                solicitacao.getCriadoEm()
        );
    }
}
