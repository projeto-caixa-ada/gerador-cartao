package org.emissao.dto;

import java.time.Instant;

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