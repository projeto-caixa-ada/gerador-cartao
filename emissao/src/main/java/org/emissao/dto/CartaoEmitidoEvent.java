package org.emissao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record CartaoEmitidoEvent(
        String solicitacaoId,
        String clienteId,
        String cartaoId,
        String numeroCartao,
        String status,
        Instant ocorridoEm
) {
}