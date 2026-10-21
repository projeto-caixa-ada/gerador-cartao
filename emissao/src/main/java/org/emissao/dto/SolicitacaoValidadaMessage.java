package org.emissao.dto;

public record SolicitacaoValidadaMessage(
        Long eventId,
        Long solicitacaoId,
        String clienteId,
        String nomeTitular
) {
}