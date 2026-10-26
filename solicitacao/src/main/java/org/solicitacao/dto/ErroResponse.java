package org.solicitacao.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Resposta de erro da API")
public record ErroResponse(
        @Schema(
                description = "Mensagem descritiva do erro",
                examples = {"clienteId é obrigatório"}
        )
        String erro
) {
}
