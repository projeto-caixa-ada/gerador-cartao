package org.solicitacao.dto;

import org.solicitacao.entity.Solicitacao;

import java.time.Instant;

/**
 * Evento publicado no Kafka quando uma solicitação é registrada.
 *
 * <p>O {@code solicitacaoId} é o identificador único da solicitação, gerado pelo
 * banco e utilizado como chave de idempotência pelo serviço consumidor.</p>
 */
public record SolicitacaoCriadaEvent(
        Long solicitacaoId,
        String clienteId,
        Instant dataSolicitacao
) {

    public static SolicitacaoCriadaEvent from(Solicitacao solicitacao) {
        return new SolicitacaoCriadaEvent(
                solicitacao.getId(),
                solicitacao.getClienteId(),
                solicitacao.getCriadoEm()
        );
    }
}
