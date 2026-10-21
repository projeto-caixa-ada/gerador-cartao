package org.emissao.service;

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
import org.emissao.repository.CartaoRepository;
import org.emissao.repository.EmissaoRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CartaoApplicationService {

    @Inject
    CartaoRepository cartaoRepository;

    @Inject
    EmissaoRepository emissaoRepository;

    @Inject
    CartaoNumberGenerator cartaoNumberGenerator;

    @Inject
    KafkaCartaoEmitidoPublisher kafkaCartaoEmitidoPublisher;

    @Transactional
    public Emissao processarSolicitacaoValidada(SolicitacaoValidadaMessage mensagem) {
       validarMensagem(mensagem);

       String solicitacaoId = String.valueOf(mensagem.solicitacaoId());
       Optional<Cartao> cartaoExistente = cartaoRepository.buscarPorSolicitacaoId(solicitacaoId);

       if (cartaoExistente.isPresent()) {
           return emissaoRepository.buscarEmissaoPorSolicitacaoId(solicitacaoId)
                   .orElseThrow(() -> new IllegalStateException("Emissão já existe para a solicitação " + solicitacaoId));
       }

       String numeroCartao = cartaoNumberGenerator.gerarNumero();

       Cartao cartao = new Cartao();
       cartao.setSolicitacaoId(solicitacaoId);
       cartao.setClienteId(mensagem.clienteId());
       cartao.setNomeTitular(mensagem.nomeTitular());
       cartao.setNumero(numeroCartao);
       cartao.setStatus(StatusCartao.EMITIDO);

       cartaoRepository.persist(cartao);

       Emissao emissao = new Emissao();
       emissao.setSolicitacaoId(solicitacaoId);
       emissao.setClienteId(mensagem.clienteId());
       emissao.setCartao(cartao);
       emissao.setStatus(StatusEmissao.EMITIDA);

       emissaoRepository.persist(emissao);

       CartaoEmitidoEvent evento = new CartaoEmitidoEvent(
               solicitacaoId,
               mensagem.clienteId(),
               String.valueOf(cartao.getId()),
               cartao.getNumero(),
               cartao.getStatus().name(),
               Instant.now()
       );

       kafkaCartaoEmitidoPublisher.publicar(evento);
       return emissao;
    }

    public Cartao buscarCartao(Long id) {
       return cartaoRepository.findById(id);
    }

    public Emissao buscarEmissao(Long id) {
       return emissaoRepository.findById(id);
    }

    private void validarMensagem(SolicitacaoValidadaMessage mensagem) {
       if (mensagem == null) {
           throw new IllegalArgumentException("Mensagem de solicitação não pode ser nula");
       }
       if (mensagem.solicitacaoId() == null) {
           throw new IllegalArgumentException("solicitacaoId é obrigatório");
       }
       if (mensagem.clienteId() == null || mensagem.clienteId().isBlank()) {
           throw new IllegalArgumentException("clienteId é obrigatório");
       }
       if (mensagem.nomeTitular() == null || mensagem.nomeTitular().isBlank()) {
           throw new IllegalArgumentException("nomeTitular é obrigatório");
       }
    }
}
