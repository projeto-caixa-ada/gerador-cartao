package org.solicitacao.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.solicitacao.entity.Solicitacao;

import java.time.Instant;

@Schema(description = "Dados de uma solicitação de cartão")
public record SolicitacaoResponse(
        @Schema(
                description = "Identificador da solicitação",
                examples = {"1"}
        )
        Long id,

        @Schema(
                description = "Identificador do cliente",
                examples = {"123"}
        )
        String clienteId,

        @Schema(
                description = "Status atual da solicitação",
                examples = {"ABERTA"},
                enumeration = {"ABERTA", "VALIDADA", "EMITIDA", "CANCELADA"}
        )
        String status,

        @Schema(
                description = "Data e hora de criação da solicitação (UTC)",
                examples = {"2026-10-25T18:00:00Z"}
        )
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
