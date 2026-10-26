package org.solicitacao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CartaoEmitidoEvent(
        String solicitacaoId,
        String clienteId,
        String cartaoId,
        String numeroCartao,
        String status,
        Instant dataSolicitacao,
        Instant ocorridoEm
) {
}
