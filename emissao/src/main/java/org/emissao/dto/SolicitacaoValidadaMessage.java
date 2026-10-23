package org.emissao.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record SolicitacaoValidadaMessage(
        @JsonProperty("solicitacaoId") Long solicitacaoId,
        @JsonProperty("clienteId") String clienteId,
        @JsonProperty("dataSolicitacao")
        @JsonAlias({"dataSolcitacao", "data_solicitacao"})
        Instant dataSolicitacao
) {
}