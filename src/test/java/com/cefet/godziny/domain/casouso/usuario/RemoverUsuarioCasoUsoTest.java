package com.cefet.godziny.domain.casouso.usuario;

import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.exceptions.usuario.RemoverUsuarioComAtividadesException;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeEntidade;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemoverUsuarioCasoUsoTest {

    private static final Integer MATRICULA = 99999;

    private UsuarioRepositorioJpa usuarioRepositorioJpa;
    private AtividadeRepositorioJpa atividadeRepositorioJpa;

    private RemoverUsuarioCasoUso removerUsuarioCasoUso;

    @BeforeEach
    void inicializarDados() {
        usuarioRepositorioJpa = mock(UsuarioRepositorioJpa.class);
        atividadeRepositorioJpa = mock(AtividadeRepositorioJpa.class);

        autenticarAdministrador();

        removerUsuarioCasoUso = new RemoverUsuarioCasoUso(
                MATRICULA,
                usuarioRepositorioJpa,
                atividadeRepositorioJpa
        );
    }

    @AfterEach
    void limparDados() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarAdministrador() {
        UsuarioEntidade administrador = new UsuarioEntidade(
                1,
                null,
                "Administrador",
                "admin@teste.com.br",
                "senha123",
                EnumRecursos.ADM,
                LocalDateTime.now()
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        administrador,
                        null,
                        Collections.emptyList()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    private void autenticarUsuarioNormal() {
        UsuarioEntidade usuario = new UsuarioEntidade(
                2,
                null,
                "Usuario Normal",
                "usuario@teste.com.br",
                "senha123",
                EnumRecursos.NORMAL,
                LocalDateTime.now()
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        Collections.emptyList()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    private UsuarioEntidade criarUsuarioEntidade() {
        UsuarioEntidade administrador = new UsuarioEntidade(
                99998,
                null,
                "Administrador do Curso",
                "admincurso@teste.com.br",
                "senha123",
                EnumRecursos.ADM,
                LocalDateTime.now()
        );

        CursoEntidade curso = new CursoEntidade(
                UUID.randomUUID(),
                "CURSO_ID_TESTE",
                "TESTE",
                100,
                administrador
        );

        return new UsuarioEntidade(
                MATRICULA,
                curso,
                "TESTE",
                "teste@teste.com.br",
                "teste123",
                EnumRecursos.NORMAL,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should validate and delete a user without activities successfully")
    void deveRemoverUsuarioSemAtividadesComSucesso() throws Exception {
        UsuarioEntidade usuario = criarUsuarioEntidade();

        when(usuarioRepositorioJpa.findById(MATRICULA))
                .thenReturn(usuario);

        when(atividadeRepositorioJpa.findByUsuario(usuario))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(
                () -> removerUsuarioCasoUso.validarRemocao()
        );

        removerUsuarioCasoUso.removerUsuario();

        verify(usuarioRepositorioJpa).findById(MATRICULA);
        verify(atividadeRepositorioJpa).findByUsuario(usuario);
        verify(usuarioRepositorioJpa).deleteUsuario(MATRICULA);
    }

    @Test
    @DisplayName("Should reject deletion when the user has registered activities")
    void deveRejeitarRemocaoDeUsuarioComAtividades() throws Exception {
        UsuarioEntidade usuario = criarUsuarioEntidade();

        when(usuarioRepositorioJpa.findById(MATRICULA))
                .thenReturn(usuario);

        when(atividadeRepositorioJpa.findByUsuario(usuario))
                .thenReturn(List.of(mock(AtividadeEntidade.class)));

        RemoverUsuarioComAtividadesException exception = assertThrows(
                RemoverUsuarioComAtividadesException.class,
                () -> removerUsuarioCasoUso.validarRemocao()
        );

        assertThat(exception.getMessage())
                .isEqualTo(
                        "Não é possível excluir um usuário que possui atividades cadastradas"
                );
    }

    @Test
    @DisplayName("Should reject deletion by a non-administrator user")
    void deveRejeitarRemocaoPorUsuarioNaoAdministrador() {
        autenticarUsuarioNormal();

        assertThrows(
                UsuarioNaoAutorizadoException.class,
                () -> removerUsuarioCasoUso.validarRemocao()
        );
    }

    @Test
    @DisplayName("Should call the repository to delete the user")
    void deveChamarRepositorioParaRemoverUsuario() throws Exception {
        removerUsuarioCasoUso.removerUsuario();

        verify(usuarioRepositorioJpa).deleteUsuario(MATRICULA);
    }
}