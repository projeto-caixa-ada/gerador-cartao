package org.solicitacao.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
        description = "Dados necessários para criar uma solicitação de cartão",
        examples = {"{\"clienteId\":\"123\"}"}
)
public record CriarSolicitacaoRequest(
        @Schema(
                description = "Identificador do cliente que está solicitando o cartão",
                examples = {"123"},
                required = true,
                maxLength = 64
        )
        String clienteId
) {
}
