package org.emissao.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Dados de um cartão emitido")
public record CartaoEmitidoEvent(
        @Schema(
                description = "Identificador da solicitação que originou o cartão",
                examples = {"1"}
        )
        String solicitacaoId,

        @Schema(
                description = "Identificador do cliente",
                examples = {"123"}
        )
        String clienteId,

        @Schema(
                description = "Identificador do cartão",
                examples = {"1"}
        )
        String cartaoId,

        @Schema(
                description = "Número gerado para o cartão",
                examples = {"1000000000000000"}
        )
        String numeroCartao,

        @Schema(
                description = "Status atual do cartão",
                examples = {"EMITIDO"},
                enumeration = {"EMITIDO", "ATIVO", "BLOQUEADO", "CANCELADO"}
        )
        String status,

        @Schema(
                description = "Data e hora da solicitação original (UTC)",
                examples = {"2026-10-25T18:00:00Z"}
        )
        Instant dataSolicitacao,

        @Schema(
                description = "Data e hora em que a emissão ocorreu (UTC)",
                examples = {"2026-10-25T18:00:02Z"}
        )
        Instant ocorridoEm
) {
}
