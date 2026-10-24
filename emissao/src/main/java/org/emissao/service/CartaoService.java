package org.emissao.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.quarkus.redis.datasource.RedisDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.emissao.dto.CartaoEmitidoEvent;
import org.emissao.dto.SolicitacaoValidadaMessage;
import org.emissao.entity.Cartao;
import org.emissao.entity.Emissao;
import org.emissao.enums.StatusCartao;
import org.emissao.enums.StatusEmissao;
import org.emissao.infrastructure.CartaoNumberGenerator;
import org.emissao.infrastructure.messaging.KafkaCartaoEmitidoPublisher;
import org.emissao.mapper.CartaoMapper;
import org.emissao.repository.CartaoRepository;
import org.emissao.repository.EmissaoRepository;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
public class CartaoService {

    private static final Logger LOG =
            Logger.getLogger(CartaoService.class);

    // Tempo de vida do cache de 20 segundos
    private static final long TTL_SEGUNDOS = 20;

    @Inject
    CartaoRepository cartaoRepository;

    @Inject
    EmissaoRepository emissaoRepository;

    @Inject
    CartaoNumberGenerator cartaoNumberGenerator;

    @Inject
    KafkaCartaoEmitidoPublisher kafkaCartaoEmitidoPublisher;

    @Inject
    RedisDataSource redis;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    CartaoMapper cartaoMapper;

    @Transactional
    public Emissao processarSolicitacaoValidada(SolicitacaoValidadaMessage mensagem) {
        validarMensagem(mensagem);

        String solicitacaoId = String.valueOf(mensagem.solicitacaoId());

        Optional<Cartao> cartaoExistente =
                cartaoRepository.buscarPorSolicitacaoId(solicitacaoId);

        if (cartaoExistente.isPresent()) {
            LOG.infof(
                    "Evento duplicado ignorado. Solicitação %s já possui cartão emitido.",
                    solicitacaoId
            );

            return emissaoRepository.buscarEmissaoPorSolicitacaoId(solicitacaoId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Cartão existente sem emissão correspondente para a solicitação "
                                    + solicitacaoId
                    ));
        }

        Instant dataSolicitacao = mensagem.dataSolicitacao() != null
                ? mensagem.dataSolicitacao()
                : Instant.now();

        String numeroCartao = cartaoNumberGenerator.gerarNumero();

        Cartao cartao = new Cartao();
        cartao.setSolicitacaoId(solicitacaoId);
        cartao.setClienteId(mensagem.clienteId());
        cartao.setDataSolicitacao(dataSolicitacao);
        cartao.setNumero(numeroCartao);
        cartao.setStatus(StatusCartao.EMITIDO);

        cartaoRepository.persist(cartao);

        Emissao emissao = new Emissao();
        emissao.setSolicitacaoId(solicitacaoId);
        emissao.setClienteId(mensagem.clienteId());
        emissao.setDataSolicitacao(dataSolicitacao);
        emissao.setCartao(cartao);
        emissao.setStatus(StatusEmissao.EMITIDA);

        emissaoRepository.persist(emissao);

        CartaoEmitidoEvent evento = new CartaoEmitidoEvent(
                solicitacaoId,
                mensagem.clienteId(),
                String.valueOf(cartao.getId()),
                cartao.getNumero(),
                cartao.getStatus().name(),
                dataSolicitacao,
                Instant.now()
        );

        kafkaCartaoEmitidoPublisher.publicar(evento);

        return emissao;
    }

    public CartaoEmitidoEvent buscarPorSolicitacaoId(String solicitacaoId) {
        String chave = "cartao:solicitacao:" + solicitacaoId;

        try {
            String json = redis.value(String.class).get(chave);
            if (json != null) {
                LOG.infof("Cache hit para a solicitação %s", solicitacaoId);
                return objectMapper.readValue(json, CartaoEmitidoEvent.class);
            }
        } catch (Exception e) {
            LOG.warnf("Falha ao consultar Redis por solicitacaoId: %s", e.getMessage());
        }

        Cartao cartao = cartaoRepository.buscarPorSolicitacaoId(solicitacaoId)
                .orElse(null);

        if (cartao == null) {
            LOG.infof("Cartão não encontrado para a solicitação %s", solicitacaoId);
            return null;
        }

        Log.info("Cartão consultado fonte de verdade para a solicitação " + solicitacaoId + ": " + (cartao != null ? cartao.getId() : "null"));

        CartaoEmitidoEvent resultado = cartaoMapper.toEvent(cartao);

        try {
            String json = objectMapper.writeValueAsString(resultado);
            redis.value(String.class).setex(chave, TTL_SEGUNDOS, json);
        } catch (Exception e) {
            LOG.warnf("Falha ao gravar no Redis: %s", e.getMessage());
        }

        return resultado;
    }

    public CartaoEmitidoEvent buscarPorId(Long cartaoId) {
        String chave = "cartao:" + cartaoId;

        try {
            String json = redis.value(String.class).get(chave);
            if (json != null) {
                LOG.infof("Cache hit para o cartão %d", cartaoId);
                return objectMapper.readValue(json, CartaoEmitidoEvent.class);
            }
        } catch (Exception e) {
            LOG.warnf("Falha ao consultar Redis por cartaoId: %s", e.getMessage());
        }

        Cartao cartao = cartaoRepository.findById(cartaoId);

        if (cartao == null) {
            LOG.infof("Cartão não encontrado para o ID %d", cartaoId);
            return null;
        }

        Log.info("Cartão consultado fonte de verdade para o ID " + cartaoId + ": " + (cartao != null ? cartao.getId() : "null"));

        CartaoEmitidoEvent resultado = cartaoMapper.toEvent(cartao);

        try {
            String json = objectMapper.writeValueAsString(resultado);
            redis.value(String.class).setex(chave, TTL_SEGUNDOS, json);
        } catch (Exception e) {
            LOG.warnf("Falha ao gravar no Redis: %s", e.getMessage());
        }

        return resultado;
    }

    @Transactional
    public void atualizarStatus(Long id, StatusCartao novoStatus) {
        Cartao cartao = cartaoRepository.findById(id);

        if (cartao == null) {
            throw new IllegalArgumentException("Cartão não encontrado");
        }

        cartao.setStatus(novoStatus);

        // Invalidação após a atualização do banco
        invalidar(id);
    }

    public void invalidar(Long id) {
        try {
            redis.key().del("cartao:" + id);
            LOG.infof("Cache invalidado para o cartão %d", id);
        } catch (Exception e) {
            LOG.warnf("Falha ao invalidar cache: %s", e.getMessage());
        }
    }

    private void validarMensagem(SolicitacaoValidadaMessage mensagem) {

        if (mensagem == null) {
            throw new IllegalArgumentException(
                    "Mensagem de solicitação não pode ser nula"
            );
        }

        if (mensagem.solicitacaoId() == null) {
            throw new IllegalArgumentException(
                    "solicitacaoId é obrigatório"
            );
        }

        if (mensagem.clienteId() == null || mensagem.clienteId().isBlank()) {
            throw new IllegalArgumentException(
                    "clienteId é obrigatório"
            );
        }
    }

}
