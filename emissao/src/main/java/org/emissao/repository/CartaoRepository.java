package org.emissao.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.emissao.entity.Cartao;

import java.util.Optional;

@ApplicationScoped
public class CartaoRepository implements PanacheRepository<Cartao> {

    public Optional<Cartao> buscarPorSolicitacaoId(String solicitacaoId) {
        return find("solicitacaoId", solicitacaoId).firstResultOptional();
    }
}
