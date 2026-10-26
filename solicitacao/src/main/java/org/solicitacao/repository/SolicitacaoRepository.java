package org.solicitacao.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.solicitacao.entity.Solicitacao;

@ApplicationScoped
public class SolicitacaoRepository implements PanacheRepository<Solicitacao> {
}
