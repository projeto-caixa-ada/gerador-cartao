package org.solicitacao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.solicitacao.enums.StatusSolicitacao;

import java.time.Instant;

@Entity
@Table(name = "solicitacao")
public class Solicitacao {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "cliente_id", nullable = false, length = 64)
    private String clienteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusSolicitacao status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    public Solicitacao() {
    }

    public Solicitacao(String id, String clienteId, StatusSolicitacao status) {
        this.id = id;
        this.clienteId = clienteId;
        this.status = status;
    }

    @PrePersist
    void onCreate() {
        if (criadoEm == null) {
            criadoEm = Instant.now();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacao status) {
        this.status = status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }
}
