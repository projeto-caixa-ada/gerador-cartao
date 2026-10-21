package org.emissao.entity;

import jakarta.persistence.*;
import org.emissao.enums.StatusEmissao;

import java.time.Instant;

@Entity
@Table(name = "emissao")
public class Emissao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public Long id;

    @Column(name = "solicitacao_id", nullable = false, unique = true)
    private String solicitacaoId;

    @Column(name = "cliente_id", nullable = false)
    private String clienteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartao_id", nullable = false)
    private Cartao cartao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusEmissao status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        criadoEm = now;
        atualizadoEm = now;
    }

    @PreUpdate
    void onUpdate() {
        atualizadoEm = Instant.now();
    }

    public Emissao() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSolicitacaoId() {
        return solicitacaoId;
    }

    public void setSolicitacaoId(String solicitacaoId) {
        this.solicitacaoId = solicitacaoId;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public Cartao getCartao() {
        return cartao;
    }

    public void setCartao(Cartao cartao) {
        this.cartao = cartao;
    }

    public StatusEmissao getStatus() {
        return status;
    }

    public void setStatus(StatusEmissao status) {
        this.status = status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(Instant atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
