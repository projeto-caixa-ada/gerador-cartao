package org.solicitacao.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.solicitacao.dto.CartaoEmitidoEvent;
import org.solicitacao.dto.SolicitacaoCriadaEvent;
import org.solicitacao.entity.Solicitacao;
import org.solicitacao.enums.StatusSolicitacao;
import org.solicitacao.exception.ValidacaoException;
import org.solicitacao.infrastructure.messaging.KafkaSolicitacaoPublisher;
import org.solicitacao.repository.SolicitacaoRepository;

@ApplicationScoped
public class SolicitacaoApplicationService {

    private static final Logger LOG = Logger.getLogger(SolicitacaoApplicationService.class);

    private static final int TAMANHO_MAXIMO_CLIENTE_ID = 64;

    @Inject
    SolicitacaoRepository solicitacaoRepository;

    @Inject
    KafkaSolicitacaoPublisher kafkaSolicitacaoPublisher;

    @Transactional
    public Solicitacao criarSolicitacao(String clienteId) {

        validarClienteId(clienteId);

        Solicitacao solicitacao = new Solicitacao(
                clienteId.trim(),
                StatusSolicitacao.ABERTA
        );

        solicitacaoRepository.persist(solicitacao);
        solicitacaoRepository.flush();

        SolicitacaoCriadaEvent evento = SolicitacaoCriadaEvent.from(solicitacao);

        kafkaSolicitacaoPublisher.publicar(evento);

        return solicitacao;
    }

    @Transactional
    public void atualizarStatusPorCartaoEmitido(CartaoEmitidoEvent evento) {
        Long solicitacaoId = extrairSolicitacaoId(evento);

        Solicitacao solicitacao = solicitacaoRepository.findById(solicitacaoId);

        if (solicitacao == null) {
            LOG.warnf(
                    "Evento cartao.emitido ignorado: solicitação %d não encontrada.",
                    solicitacaoId
            );
            return;
        }

        if (solicitacao.getStatus() != StatusSolicitacao.ABERTA) {
            LOG.infof(
                    "Evento cartao.emitido ignorado: solicitação %d está com status %s, "
                            + "somente solicitações ABERTAS podem ser atualizadas.",
                    solicitacaoId,
                    solicitacao.getStatus()
            );
            return;
        }

        solicitacao.setStatus(StatusSolicitacao.EMITIDA);

        LOG.infof(
                "Solicitação %d atualizada para %s após receber o evento cartao.emitido.",
                solicitacaoId,
                StatusSolicitacao.EMITIDA
        );
    }

    private Long extrairSolicitacaoId(CartaoEmitidoEvent evento) {
        if (evento == null) {
            throw new IllegalArgumentException("Evento de cartão emitido não pode ser nulo");
        }

        if (evento.solicitacaoId() == null || evento.solicitacaoId().isBlank()) {
            throw new IllegalArgumentException(
                    "solicitacaoId é obrigatório no evento cartao.emitido"
            );
        }

        try {
            return Long.valueOf(evento.solicitacaoId().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "solicitacaoId inválido no evento cartao.emitido: " + evento.solicitacaoId(),
                    e
            );
        }
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
