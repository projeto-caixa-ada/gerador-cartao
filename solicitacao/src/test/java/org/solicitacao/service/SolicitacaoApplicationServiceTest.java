package org.solicitacao.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.solicitacao.dto.CartaoEmitidoEvent;
import org.solicitacao.dto.SolicitacaoCriadaEvent;
import org.solicitacao.entity.Solicitacao;
import org.solicitacao.enums.StatusSolicitacao;
import org.solicitacao.exception.RecursoNaoEncontradoException;
import org.solicitacao.exception.ValidacaoException;
import org.solicitacao.infrastructure.messaging.KafkaSolicitacaoPublisher;
import org.solicitacao.repository.SolicitacaoRepository;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitacaoApplicationServiceTest {

    @Mock
    SolicitacaoRepository solicitacaoRepository;

    @Mock
    KafkaSolicitacaoPublisher kafkaSolicitacaoPublisher;

    SolicitacaoApplicationService service;

    @BeforeEach
    void setUp() {
        service = new SolicitacaoApplicationService();
        service.solicitacaoRepository = solicitacaoRepository;
        service.kafkaSolicitacaoPublisher = kafkaSolicitacaoPublisher;
    }

    @Test
    void criarSolicitacaoPersistePublicaEventoERetornaSolicitacao() {
        Instant criadoEm = Instant.parse("2026-10-26T10:00:00Z");

        doAnswer(invocation -> {
            Solicitacao persistida = invocation.getArgument(0);
            persistida.setId(42L);
            persistida.setCriadoEm(criadoEm);
            return null;
        }).when(solicitacaoRepository).persist(any(Solicitacao.class));

        Solicitacao solicitacao = service.criarSolicitacao("  cliente-1  ");

        assertEquals("cliente-1", solicitacao.getClienteId());
        assertEquals(StatusSolicitacao.ABERTA, solicitacao.getStatus());
        assertEquals(42L, solicitacao.getId());

        verify(solicitacaoRepository).persist(solicitacao);
        verify(solicitacaoRepository).flush();

        ArgumentCaptor<SolicitacaoCriadaEvent> captor =
                ArgumentCaptor.forClass(SolicitacaoCriadaEvent.class);
        verify(kafkaSolicitacaoPublisher).publicar(captor.capture());

        SolicitacaoCriadaEvent evento = captor.getValue();
        assertEquals(42L, evento.solicitacaoId());
        assertEquals("cliente-1", evento.clienteId());
        assertEquals(criadoEm, evento.dataSolicitacao());
    }

    @Test
    void criarSolicitacaoAceitaClienteIdNoLimiteDe64Caracteres() {
        String clienteId = "a".repeat(64);

        Solicitacao solicitacao = service.criarSolicitacao(clienteId);

        assertEquals(clienteId, solicitacao.getClienteId());
        verify(kafkaSolicitacaoPublisher).publicar(any(SolicitacaoCriadaEvent.class));
    }

    @Test
    void criarSolicitacaoRejeitaClienteIdNuloOuEmBranco() {
        assertThrows(ValidacaoException.class, () -> service.criarSolicitacao(null));
        assertThrows(ValidacaoException.class, () -> service.criarSolicitacao(""));
        assertThrows(ValidacaoException.class, () -> service.criarSolicitacao("   "));
        verify(solicitacaoRepository, never()).persist(any(Solicitacao.class));
    }

    @Test
    void criarSolicitacaoRejeitaClienteIdMaiorQue64Caracteres() {
        String clienteId = "a".repeat(65);

        ValidacaoException exception = assertThrows(
                ValidacaoException.class,
                () -> service.criarSolicitacao(clienteId)
        );

        assertTrue(exception.getMessage().contains("64"));
    }

    @Test
    void buscarPorIdRetornaSolicitacaoExistente() {
        Solicitacao solicitacao = new Solicitacao("cliente", StatusSolicitacao.ABERTA);
        when(solicitacaoRepository.findByIdOptional(7L)).thenReturn(Optional.of(solicitacao));

        assertSame(solicitacao, service.buscarPorId(7L));
    }

    @Test
    void buscarPorIdRejeitaIdInvalido() {
        assertThrows(ValidacaoException.class, () -> service.buscarPorId(null));
        assertThrows(ValidacaoException.class, () -> service.buscarPorId(0L));
        assertThrows(ValidacaoException.class, () -> service.buscarPorId(-1L));
    }

    @Test
    void buscarPorIdLancaRecursoNaoEncontradoQuandoNaoExiste() {
        when(solicitacaoRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> service.buscarPorId(99L)
        );

        assertTrue(exception.getMessage().contains("99"));
    }

    @Test
    void atualizarStatusMarcaSolicitacaoAbertaComoEmitida() {
        Solicitacao solicitacao = new Solicitacao("cliente", StatusSolicitacao.ABERTA);
        when(solicitacaoRepository.findById(42L)).thenReturn(solicitacao);

        service.atualizarStatusPorCartaoEmitido(evento(" 42 "));

        assertEquals(StatusSolicitacao.EMITIDA, solicitacao.getStatus());
    }

    @Test
    void atualizarStatusIgnoraSolicitacaoInexistente() {
        when(solicitacaoRepository.findById(42L)).thenReturn(null);

        service.atualizarStatusPorCartaoEmitido(evento("42"));

        verify(solicitacaoRepository, never()).persist(any(Solicitacao.class));
    }

    @Test
    void atualizarStatusIgnoraSolicitacaoQueNaoEstaAberta() {
        Solicitacao solicitacao = new Solicitacao("cliente", StatusSolicitacao.EMITIDA);
        when(solicitacaoRepository.findById(42L)).thenReturn(solicitacao);

        service.atualizarStatusPorCartaoEmitido(evento("42"));

        assertEquals(StatusSolicitacao.EMITIDA, solicitacao.getStatus());
    }

    @Test
    void atualizarStatusIgnoraSolicitacaoComStatusNulo() {
        Solicitacao solicitacao = new Solicitacao("cliente", null);
        when(solicitacaoRepository.findById(42L)).thenReturn(solicitacao);

        service.atualizarStatusPorCartaoEmitido(evento("42"));

        assertEquals(null, solicitacao.getStatus());
    }

    @Test
    void atualizarStatusRejeitaEventoNulo() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarStatusPorCartaoEmitido(null)
        );

        assertEquals("Evento de cartão emitido não pode ser nulo", exception.getMessage());
    }

    @Test
    void atualizarStatusRejeitaSolicitacaoIdNuloOuEmBranco() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarStatusPorCartaoEmitido(evento(null))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarStatusPorCartaoEmitido(evento("  "))
        );
    }

    @Test
    void atualizarStatusRejeitaSolicitacaoIdNaoNumerico() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarStatusPorCartaoEmitido(evento("abc"))
        );

        assertTrue(exception.getMessage().contains("abc"));
        assertNotNull(exception.getCause());
    }

    private CartaoEmitidoEvent evento(String solicitacaoId) {
        return new CartaoEmitidoEvent(
                solicitacaoId,
                "cliente",
                "5001",
                "1000000000000000",
                "EMITIDO",
                Instant.parse("2026-10-26T10:00:00Z"),
                Instant.parse("2026-10-26T10:00:05Z")
        );
    }
}
