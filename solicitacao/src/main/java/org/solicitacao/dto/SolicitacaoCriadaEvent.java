package org.solicitacao.dto;

import org.solicitacao.entity.Solicitacao;

import java.time.Instant;

/**
 * Evento publicado no Kafka quando uma solicitação é registrada.
 *
 * <p>O {@code solicitacaoId} é o identificador único da solicitação e também
 * a chave de idempotência usada pelo serviço consumidor. O {@code eventId}
 * identifica a mensagem, permitindo descartar reentregas do mesmo evento.</p>
 */
public record SolicitacaoCriadaEvent(
        String eventId,
        String solicitacaoId,
        String clienteId,
        Instant dataSolicitacao
) {

    public static SolicitacaoCriadaEvent from(Solicitacao solicitacao, String eventId) {
        return new SolicitacaoCriadaEvent(
                eventId,
                solicitacao.getId(),
                solicitacao.getClienteId(),
                solicitacao.getCriadoEm()
        );
    }
}
