package org.emissao.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.emissao.dto.CartaoEmitidoEvent;
import org.emissao.entity.Cartao;

import java.time.Instant;

@ApplicationScoped
public class CartaoMapper {

    public static CartaoEmitidoEvent toEvent(Cartao cartao) {
        if (cartao == null) {
            return null;
        }

        return new CartaoEmitidoEvent(
                cartao.getSolicitacaoId(),
                cartao.getClienteId(),
                String.valueOf(cartao.getId()),
                cartao.getNumero(),
                cartao.getStatus() != null ? cartao.getStatus().name() : null,
                cartao.getDataSolicitacao(),
                Instant.now()
        );
    }
}