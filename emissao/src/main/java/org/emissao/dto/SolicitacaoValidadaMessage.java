package org.emissao.dto;

public record SolicitacaoValidadaMessage(
        Long solicitacaoId,
        String clienteId,
        String nomeTitular
) {
}