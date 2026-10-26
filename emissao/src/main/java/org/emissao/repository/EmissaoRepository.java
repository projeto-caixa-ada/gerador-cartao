package org.emissao.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.emissao.entity.Emissao;

import java.util.Optional;

@ApplicationScoped
public class EmissaoRepository implements PanacheRepository<Emissao> {

    public Optional<Emissao> buscarEmissaoPorSolicitacaoId(String solicitacaoId) {
        return find("solicitacaoId", solicitacaoId).firstResultOptional();
    }
}
