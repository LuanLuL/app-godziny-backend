package com.cefet.godziny.domain.casouso.usuario;

import com.cefet.godziny.api.usuario.UsuarioDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.exceptions.usuario.CriarUsuarioAdmComCursoException;
import com.cefet.godziny.infraestrutura.exceptions.usuario.CriarUsuarioEmailRepetidoException;
import com.cefet.godziny.infraestrutura.exceptions.usuario.CriarUsuarioIncompletoException;
import com.cefet.godziny.infraestrutura.exceptions.usuario.CriarUsuarioNormalSemCursoException;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AtualizarUsuarioCasoUsoTest {

    private UsuarioRepositorioJpa usuarioRepositorioJpa;
    private CursoRepositorioJpa cursoRepositorioJpa;

    private AtualizarUsuarioCasoUso atualizarUsuarioCasoUso;

    @BeforeEach
    void setUp() {
        usuarioRepositorioJpa = mock(UsuarioRepositorioJpa.class);
        cursoRepositorioJpa = mock(CursoRepositorioJpa.class);

        autenticarAdministrador();

        atualizarUsuarioCasoUso = new AtualizarUsuarioCasoUso(
                999999,
                "TESTE",
                "teste@teste.com.br",
                "teste123",
                "cursoSIGLA",
                LocalDateTime.now(),
                EnumRecursos.NORMAL,
                usuarioRepositorioJpa,
                cursoRepositorioJpa
        );
    }

    @AfterEach
    void tearDown() {
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

    private UsuarioDto criarUsuarioDto() {
        return new UsuarioDto(
                999999,
                "cursoSIGLA",
                "TESTE",
                "teste@teste.com.br",
                "teste123",
                EnumRecursos.NORMAL,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should validate a user update successfully")
    void deveValidarAtualizacaoComSucesso() throws Exception {
        when(usuarioRepositorioJpa.findByEmail("teste@teste.com.br"))
                .thenReturn(null);

        assertDoesNotThrow(
                () -> atualizarUsuarioCasoUso.validarAtualizacao()
        );
    }

    @Test
    @DisplayName("Should reject updates from non-administrator users")
    void deveImpedirAtualizacaoPorUsuarioNaoAdministrador() {
        autenticarUsuarioNormal();

        assertThrows(
                UsuarioNaoAutorizadoException.class,
                () -> atualizarUsuarioCasoUso.validarAtualizacao()
        );
    }

    @Test
    @DisplayName("Should reject a name shorter than three characters")
    void deveRejeitarNomeMuitoCurto() {
        atualizarUsuarioCasoUso.setNome("AB");

        assertThrows(
                CriarUsuarioIncompletoException.class,
                () -> atualizarUsuarioCasoUso.validarAtualizacao()
        );
    }

    @Test
    @DisplayName("Should reject a name longer than one hundred characters")
    void deveRejeitarNomeMuitoLongo() {
        atualizarUsuarioCasoUso.setNome("A".repeat(101));

        assertThrows(
                CriarUsuarioIncompletoException.class,
                () -> atualizarUsuarioCasoUso.validarAtualizacao()
        );
    }

    @Test
    @DisplayName("Should reject a password shorter than six characters")
    void deveRejeitarSenhaMuitoCurta() {
        atualizarUsuarioCasoUso.setSenha("12345");

        assertThrows(
                CriarUsuarioIncompletoException.class,
                () -> atualizarUsuarioCasoUso.validarAtualizacao()
        );
    }

    @Test
    @DisplayName("Should reject an invalid email address")
    void deveRejeitarEmailInvalido() {
        atualizarUsuarioCasoUso.setEmail("email-invalido");

        assertThrows(
                CriarUsuarioIncompletoException.class,
                () -> atualizarUsuarioCasoUso.validarAtualizacao()
        );
    }

    @Test
    @DisplayName("Should reject a null email address")
    void deveRejeitarEmailNulo() {
        atualizarUsuarioCasoUso.setEmail(null);

        assertThrows(
                CriarUsuarioIncompletoException.class,
                () -> atualizarUsuarioCasoUso.validarAtualizacao()
        );
    }

    @Test
    @DisplayName("Should reject an email already used by another user")
    void deveRejeitarEmailJaUtilizadoPorOutroUsuario() throws Exception {
        UsuarioEntidade usuarioExistente = new UsuarioEntidade(
                123456,
                null,
                "Outro Usuario",
                "teste@teste.com.br",
                "senha123",
                EnumRecursos.NORMAL,
                LocalDateTime.now()
        );

        when(usuarioRepositorioJpa.findByEmail("teste@teste.com.br"))
                .thenReturn(usuarioExistente);

        assertThrows(
                CriarUsuarioEmailRepetidoException.class,
                () -> atualizarUsuarioCasoUso.validarAtualizacao()
        );
    }

    @Test
    @DisplayName("Should allow the current user's email address")
    void devePermitirEmailDoProprioUsuario() throws Exception {
        UsuarioEntidade usuarioExistente = new UsuarioEntidade(
                999999,
                null,
                "TESTE",
                "teste@teste.com.br",
                "teste123",
                EnumRecursos.NORMAL,
                LocalDateTime.now()
        );

        when(usuarioRepositorioJpa.findByEmail("teste@teste.com.br"))
                .thenReturn(usuarioExistente);

        assertDoesNotThrow(
                () -> atualizarUsuarioCasoUso.validarAtualizacao()
        );
    }

    @Test
    @DisplayName("Should reject a creation date in the future")
    void deveRejeitarDataDeCriacaoFutura() {
        atualizarUsuarioCasoUso.setCreatedAt(
                LocalDateTime.now().plusDays(1)
        );

        assertThrows(
                CriarUsuarioIncompletoException.class,
                () -> atualizarUsuarioCasoUso.validarAtualizacao()
        );
    }

    @Test
    @DisplayName("Should reject a regular user without a course")
    void deveRejeitarUsuarioNormalSemCurso() {
        atualizarUsuarioCasoUso.setTipo(EnumRecursos.NORMAL);
        atualizarUsuarioCasoUso.setCursoSigla("");

        assertThrows(
                CriarUsuarioNormalSemCursoException.class,
                () -> atualizarUsuarioCasoUso.validarAtualizacao()
        );
    }

    @Test
    @DisplayName("Should reject an administrator associated with a course")
    void deveRejeitarAdministradorComCurso() {
        atualizarUsuarioCasoUso.setTipo(EnumRecursos.ADM);
        atualizarUsuarioCasoUso.setCursoSigla("SIGLA");

        assertThrows(
                CriarUsuarioAdmComCursoException.class,
                () -> atualizarUsuarioCasoUso.validarAtualizacao()
        );
    }

    @Test
    @DisplayName("Should allow an administrator without a course")
    void devePermitirAdministradorSemCurso() throws Exception {
        atualizarUsuarioCasoUso.setTipo(EnumRecursos.ADM);
        atualizarUsuarioCasoUso.setCursoSigla("");

        when(usuarioRepositorioJpa.findByEmail("teste@teste.com.br"))
                .thenReturn(null);

        assertDoesNotThrow(
                () -> atualizarUsuarioCasoUso.validarAtualizacao()
        );
    }

    @Test
    @DisplayName("Should convert the DTO and update the user")
    void deveConverterDtoEAtualizarUsuario() throws Exception {
        UsuarioDto dto = criarUsuarioDto();

        when(cursoRepositorioJpa.findBySigla("cursoSIGLA"))
                .thenReturn(null);

        when(usuarioRepositorioJpa.updateUsuario(any(UsuarioEntidade.class)))
                .thenReturn(999999);

        Integer resultado = atualizarUsuarioCasoUso.AtualizarUsuario(dto);

        assertEquals(999999, resultado);

        verify(cursoRepositorioJpa).findBySigla("cursoSIGLA");
        verify(usuarioRepositorioJpa).updateUsuario(
                any(UsuarioEntidade.class)
        );
    }
}