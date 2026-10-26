package org.emissao.infrastructure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartaoNumberGeneratorTest {

    private final CartaoNumberGenerator generator = new CartaoNumberGenerator();

    @Test
    void geraNumerosSequenciaisCom16Digitos() {
        String primeiro = generator.gerarNumero();
        String segundo = generator.gerarNumero();

        assertEquals(16, primeiro.length());
        assertEquals(16, segundo.length());
        assertTrue(primeiro.chars().allMatch(Character::isDigit));
        assertTrue(segundo.chars().allMatch(Character::isDigit));
        assertNotEquals(primeiro, segundo);
        assertEquals(Long.parseLong(primeiro) + 1, Long.parseLong(segundo));
    }
}
