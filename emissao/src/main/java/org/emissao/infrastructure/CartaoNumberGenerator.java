package org.emissao.infrastructure;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class CartaoNumberGenerator {

    private static final AtomicLong SEQUENCE = new AtomicLong(1000000000000000L);

    public String gerarNumero() {
        long next = SEQUENCE.getAndAdd(1);
        return String.format("%016d", next).replace(" ", "0");
    }
}
