package org.emissao.entity;

import jakarta.persistence.*;
import org.emissao.enums.StatusCartao;

import java.time.Instant;

@Entity
@Table(name = "cartao")
public class Cartao  {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public Long id;

    @Column(name = "numero", nullable = false, unique = true, length = 19)
    private String numero;

    @Column(name = "solicitacao_id", nullable = false, unique = true)
    private String solicitacaoId;

    @Column(name = "cliente_id", nullable = false)
    private String clienteId;

    @Column(name = "nome_titular", nullable = false)
    private String nomeTitular;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusCartao status;

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

    public Cartao() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
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

    public String getNomeTitular() {
        return nomeTitular;
    }

    public void setNomeTitular(String nomeTitular) {
        this.nomeTitular = nomeTitular;
    }

    public StatusCartao getStatus() {
        return status;
    }

    public void setStatus(StatusCartao status) {
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
