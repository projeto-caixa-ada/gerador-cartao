package org.solicitacao.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.solicitacao.dto.SolicitacaoCriadaEvent;
import org.solicitacao.entity.Solicitacao;
import org.solicitacao.enums.StatusSolicitacao;
import org.solicitacao.exception.ValidacaoException;
import org.solicitacao.infrastructure.messaging.KafkaSolicitacaoPublisher;
import org.solicitacao.repository.SolicitacaoRepository;

import java.util.UUID;

@ApplicationScoped
public class SolicitacaoApplicationService {

    private static final int TAMANHO_MAXIMO_CLIENTE_ID = 64;

    @Inject
    SolicitacaoRepository solicitacaoRepository;

    @Inject
    KafkaSolicitacaoPublisher kafkaSolicitacaoPublisher;

    /**
     * Registra a solicitação e publica o evento no Kafka na mesma transação.
     *
     * <p>Se a publicação falhar mesmo após as tentativas do fault tolerance,
     * a transação é revertida e nenhuma solicitação fica registrada sem que o
     * evento tenha sido enviado.</p>
     */
    @Transactional
    public Solicitacao criarSolicitacao(String clienteId) {
        validarClienteId(clienteId);

        Solicitacao solicitacao = new Solicitacao(
                UUID.randomUUID().toString(),
                clienteId.trim(),
                StatusSolicitacao.ABERTA
        );

        solicitacaoRepository.persist(solicitacao);
        solicitacaoRepository.flush();

        SolicitacaoCriadaEvent evento = SolicitacaoCriadaEvent.from(
                solicitacao,
                UUID.randomUUID().toString()
        );
        kafkaSolicitacaoPublisher.publicar(evento);

        return solicitacao;
    }

    private void validarClienteId(String clienteId) {
        if (clienteId == null || clienteId.isBlank()) {
            throw new ValidacaoException("clienteId é obrigatório");
        }
        if (clienteId.trim().length() > TAMANHO_MAXIMO_CLIENTE_ID) {
            throw new ValidacaoException(
                    "clienteId deve ter no máximo " + TAMANHO_MAXIMO_CLIENTE_ID + " caracteres");
        }
    }
}
