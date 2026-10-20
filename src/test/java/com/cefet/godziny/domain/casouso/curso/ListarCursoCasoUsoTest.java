package com.cefet.godziny.domain.casouso.curso;

import com.cefet.godziny.api.curso.CursoRecuperarDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.rest.usuario.UsuarioRestConverter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ListarCursoCasoUsoTest {

    private static final Integer MATRICULA_ADMIN = 99999;
    private static final String SIGLA_CURSO = "ODONT_DIV";
    private static final String NOME_CURSO = "Odontologia";
    private static final Integer CARGA_HORARIA = 100;

    private CursoRepositorioJpa cursoRepositorioJpa;

    private ListarCursoCasoUso listarCursoCasoUso;

    private UsuarioEntidade administrador;
    private CursoEntidade cursoEntidade;

    @BeforeEach
    void inicializarDados() {
        cursoRepositorioJpa = mock(CursoRepositorioJpa.class);

        administrador = criarAdministrador();
        cursoEntidade = criarCursoEntidade();

        autenticarAdministrador();

        listarCursoCasoUso = new ListarCursoCasoUso(
                cursoRepositorioJpa,
                SIGLA_CURSO
        );
    }

    @AfterEach
    void limparDados() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarAdministrador() {
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
        UsuarioEntidade usuarioNormal = new UsuarioEntidade(
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
                        usuarioNormal,
                        null,
                        Collections.emptyList()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    private UsuarioEntidade criarAdministrador() {
        return new UsuarioEntidade(
                MATRICULA_ADMIN,
                null,
                "Administrador",
                "admin@teste.com.br",
                "senha123",
                EnumRecursos.ADM,
                LocalDateTime.now()
        );
    }

    private CursoEntidade criarCursoEntidade() {
        return new CursoEntidade(
                UUID.randomUUID(),
                SIGLA_CURSO,
                NOME_CURSO,
                CARGA_HORARIA,
                administrador
        );
    }

    @Test
    @DisplayName("Should list a course successfully")
    void deveListarCursoComSucesso() throws Exception {
        when(cursoRepositorioJpa.findBySigla(SIGLA_CURSO))
                .thenReturn(cursoEntidade);

        assertDoesNotThrow(
                () -> listarCursoCasoUso.validarListagem()
        );

        CursoRecuperarDto resultado =
                listarCursoCasoUso.validarListagem();

        CursoRecuperarDto dtoEsperado =
                criarDtoEsperado();

        assertThat(resultado).isNotNull();
        assertThat(resultado)
                .usingRecursiveComparison()
                .isEqualTo(dtoEsperado);

        verify(cursoRepositorioJpa)
                .findBySigla(SIGLA_CURSO);
    }

    @Test
    @DisplayName("Should reject course listing by a non-administrator")
    void deveRejeitarListagemPorUsuarioNaoAdministrador() {
        autenticarUsuarioNormal();

        assertThrows(
                UsuarioNaoAutorizadoException.class,
                () -> listarCursoCasoUso.validarListagem()
        );

        verifyNoInteractions(cursoRepositorioJpa);
    }

    @Test
    @DisplayName("Should retrieve the course using its acronym")
    void deveBuscarCursoPelaSigla() throws Exception {
        when(cursoRepositorioJpa.findBySigla(SIGLA_CURSO))
                .thenReturn(cursoEntidade);

        listarCursoCasoUso.validarListagem();

        verify(cursoRepositorioJpa)
                .findBySigla(SIGLA_CURSO);
    }

    @Test
    @DisplayName("Should throw an exception when the course does not exist")
    void deveLancarExcecaoQuandoCursoNaoExistir() throws Exception{
        when(cursoRepositorioJpa.findBySigla(SIGLA_CURSO))
                .thenReturn(null);

        assertThrows(
                NullPointerException.class,
                () -> listarCursoCasoUso.validarListagem()
        );

        verify(cursoRepositorioJpa)
                .findBySigla(SIGLA_CURSO);
    }

    @Test
    @DisplayName("Should validate course listing without repository access when unauthorized")
    void deveValidarAutorizacaoAntesDeBuscarCurso() {
        autenticarUsuarioNormal();

        assertThrows(
                UsuarioNaoAutorizadoException.class,
                () -> listarCursoCasoUso.validarListagem()
        );

        verifyNoInteractions(cursoRepositorioJpa);
    }

    private CursoRecuperarDto criarDtoEsperado() {
        CursoRecuperarDto dto = new CursoRecuperarDto();

        dto.setId(cursoEntidade.getId());
        dto.setSigla(cursoEntidade.getSigla());
        dto.setNome(cursoEntidade.getNome());
        dto.setCarga_horaria_complementar(
                cursoEntidade.getCarga_horaria_complementar()
        );
        dto.setCoordenador(
                UsuarioRestConverter.EntidadeToUsuarioRecuperarDto(
                        cursoEntidade.getCoordenador()
                )
        );

        return dto;
    }
}