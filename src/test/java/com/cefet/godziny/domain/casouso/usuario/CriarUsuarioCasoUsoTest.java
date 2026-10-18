package com.cefet.godziny.domain.casouso.usuario;

import com.cefet.godziny.api.usuario.UsuarioDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.exceptions.usuario.CriarUsuarioAdmComCursoException;
import com.cefet.godziny.infraestrutura.exceptions.usuario.CriarUsuarioEmailRepetidoException;
import com.cefet.godziny.infraestrutura.exceptions.usuario.CriarUsuarioIncompletoException;
import com.cefet.godziny.infraestrutura.exceptions.usuario.CriarUsuarioNormalSemCursoException;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CriarUsuarioCasoUsoTest {

    private UsuarioDto usuarioDto;
    private CursoEntidade cursoEntidade;

    private CursoRepositorioJpa cursoRepositorioJpa;
    private UsuarioRepositorioJpa usuarioRepositorioJpa;

    private CriarUsuarioCasoUso criarUsuarioCasoUso;

    @BeforeEach
    void inicializarDados() {
        cursoRepositorioJpa = mock(CursoRepositorioJpa.class);
        usuarioRepositorioJpa = mock(UsuarioRepositorioJpa.class);

        autenticarAdministrador();

        criarUsuarioCasoUso = new CriarUsuarioCasoUso(
                "TESTE",
                "teste@teste.com.br",
                "teste123",
                "cursoSIGLA",
                LocalDateTime.now(),
                EnumRecursos.NORMAL,
                usuarioRepositorioJpa,
                cursoRepositorioJpa
        );

        usuarioDto = createUsuarioDto();
        cursoEntidade = createCursoEntidade();
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

    private UsuarioDto createUsuarioDto() {
        return new UsuarioDto(
                999999,
                "CURSO_ID_TESTE",
                "TESTE",
                "teste@teste.com.br",
                "teste123",
                EnumRecursos.NORMAL,
                LocalDateTime.now()
        );
    }

    private CursoEntidade createCursoEntidade() {
        UsuarioEntidade administrador = new UsuarioEntidade(
                99999,
                null,
                "Nome TESTE",
                "teste@test.com",
                "senha TESTE",
                EnumRecursos.ADM,
                LocalDateTime.now()
        );

        return new CursoEntidade(
                UUID.randomUUID(),
                "CURSO_ID_TESTE",
                "TESTE",
                100,
                administrador
        );
    }

    @Test
    @DisplayName("Should create an administrator user successfully")
    void deveCriarUsuarioAdministradorComSucesso() throws Exception {
        criarUsuarioCasoUso.setTipo(EnumRecursos.ADM);
        criarUsuarioCasoUso.setCursoSigla("");

        configurarMocksCriacao();

        assertDoesNotThrow(
                () -> criarUsuarioCasoUso.validarCriacao()
        );

        Integer response = criarUsuarioCasoUso.createUsuario(usuarioDto);

        assertThat(response).isEqualTo(99999);

        verificarConversaoEGravacao();
    }

    @Test
    @DisplayName("Should create a regular user successfully")
    void deveCriarUsuarioNormalComSucesso() throws Exception {
        configurarMocksCriacao();

        assertDoesNotThrow(
                () -> criarUsuarioCasoUso.validarCriacao()
        );

        Integer response = criarUsuarioCasoUso.createUsuario(usuarioDto);

        assertThat(response).isEqualTo(99999);

        verificarConversaoEGravacao();
    }

    @Test
    @DisplayName("Should reject user creation by a non-administrator")
    void deveRejeitarCriacaoPorUsuarioNaoAdministrador() {
        autenticarUsuarioNormal();

        assertThrows(
                UsuarioNaoAutorizadoException.class,
                () -> criarUsuarioCasoUso.validarCriacao()
        );
    }

    @Test
    @DisplayName("Should reject a name longer than one hundred characters")
    void deveRejeitarNomeMuitoLongo() {
        criarUsuarioCasoUso.setNome("A".repeat(101));

        CriarUsuarioIncompletoException exception = assertThrows(
                CriarUsuarioIncompletoException.class,
                () -> criarUsuarioCasoUso.validarCriacao()
        );

        assertThat(exception.getMessage())
                .isEqualTo(
                        "O nome do usuário deve ter entre 3 e 100 caracteres"
                );
    }

    @Test
    @DisplayName("Should reject a name shorter than three characters")
    void deveRejeitarNomeMuitoCurto() {
        criarUsuarioCasoUso.setNome("AB");

        CriarUsuarioIncompletoException exception = assertThrows(
                CriarUsuarioIncompletoException.class,
                () -> criarUsuarioCasoUso.validarCriacao()
        );

        assertThat(exception.getMessage())
                .isEqualTo(
                        "O nome do usuário deve ter entre 3 e 100 caracteres"
                );
    }

    @Test
    @DisplayName("Should reject a password shorter than six characters")
    void deveRejeitarSenhaMuitoCurta() {
        criarUsuarioCasoUso.setSenha("123");

        CriarUsuarioIncompletoException exception = assertThrows(
                CriarUsuarioIncompletoException.class,
                () -> criarUsuarioCasoUso.validarCriacao()
        );

        assertThat(exception.getMessage())
                .isEqualTo("A senha deve ter no mínimo 6 caracteres");
    }

    @Test
    @DisplayName("Should reject a null email address")
    void deveRejeitarEmailNulo() {
        criarUsuarioCasoUso.setEmail(null);

        CriarUsuarioIncompletoException exception = assertThrows(
                CriarUsuarioIncompletoException.class,
                () -> criarUsuarioCasoUso.validarCriacao()
        );

        assertThat(exception.getMessage())
                .isEqualTo(
                        "O email fornecido para o usuário não é válido"
                );
    }

    @Test
    @DisplayName("Should reject an invalid email address")
    void deveRejeitarEmailInvalido() {
        criarUsuarioCasoUso.setEmail("testehotmail.com");

        CriarUsuarioIncompletoException exception = assertThrows(
                CriarUsuarioIncompletoException.class,
                () -> criarUsuarioCasoUso.validarCriacao()
        );

        assertThat(exception.getMessage())
                .isEqualTo(
                        "O email fornecido para o usuário não é válido"
                );
    }

    @Test
    @DisplayName("Should reject an email address that is already registered")
    void deveRejeitarEmailDuplicado() {
        UsuarioEntidade usuarioExistente = new UsuarioEntidade(
                123456,
                cursoEntidade,
                "Outro Usuario",
                "teste@teste.com.br",
                "senha123",
                EnumRecursos.NORMAL,
                LocalDateTime.now()
        );

        when(usuarioRepositorioJpa.findByEmailOptional(
                "teste@teste.com.br"
        )).thenReturn(Optional.of(usuarioExistente));

        CriarUsuarioEmailRepetidoException exception = assertThrows(
                CriarUsuarioEmailRepetidoException.class,
                () -> criarUsuarioCasoUso.validarCriacao()
        );

        assertThat(exception.getMessage())
                .isEqualTo(
                        "O email fornecido já está cadastrado no sistema"
                );
    }

    @Test
    @DisplayName("Should reject a creation date in the future")
    void deveRejeitarDataFutura() {
        criarUsuarioCasoUso.setCreatedAt(
                LocalDateTime.now().plusMonths(1)
        );

        CriarUsuarioIncompletoException exception = assertThrows(
                CriarUsuarioIncompletoException.class,
                () -> criarUsuarioCasoUso.validarCriacao()
        );

        assertThat(exception.getMessage())
                .isEqualTo(
                        "A data de criação do usuário deve ser menor ou igual à data e hora atuais"
                );
    }

    @Test
    @DisplayName("Should reject a regular user without a course")
    void deveRejeitarUsuarioNormalSemCurso() {
        criarUsuarioCasoUso.setTipo(EnumRecursos.NORMAL);
        criarUsuarioCasoUso.setCursoSigla("");

        CriarUsuarioNormalSemCursoException exception = assertThrows(
                CriarUsuarioNormalSemCursoException.class,
                () -> criarUsuarioCasoUso.validarCriacao()
        );

        assertThat(exception.getMessage())
                .isEqualTo(
                        "Usuários do tipo 'NORMAL' devem ser associados a um curso"
                );
    }

    @Test
    @DisplayName("Should reject an administrator associated with a course")
    void deveRejeitarAdministradorComCurso() {
        criarUsuarioCasoUso.setTipo(EnumRecursos.ADM);
        criarUsuarioCasoUso.setCursoSigla("CURSO_ID_TESTE");

        CriarUsuarioAdmComCursoException exception = assertThrows(
                CriarUsuarioAdmComCursoException.class,
                () -> criarUsuarioCasoUso.validarCriacao()
        );

        assertThat(exception.getMessage())
                .isEqualTo(
                        "Usuários do tipo 'ADM' não podem ser associados a um curso"
                );
    }

    @Test
    @DisplayName("Should allow an administrator without a course")
    void devePermitirAdministradorSemCurso() {
        criarUsuarioCasoUso.setTipo(EnumRecursos.ADM);
        criarUsuarioCasoUso.setCursoSigla("");

        when(usuarioRepositorioJpa.findByEmailOptional(anyString()))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(
                () -> criarUsuarioCasoUso.validarCriacao()
        );
    }

    @Test
    @DisplayName("Should convert the DTO and persist the user")
    void deveConverterDtoEPersistirUsuario() throws Exception {
        configurarMocksCriacao();

        Integer response = criarUsuarioCasoUso.createUsuario(usuarioDto);

        assertThat(response).isEqualTo(99999);

        verificarConversaoEGravacao();
    }

    private void configurarMocksCriacao() throws Exception {
        when(cursoRepositorioJpa.findBySigla("CURSO_ID_TESTE"))
                .thenReturn(cursoEntidade);

        when(usuarioRepositorioJpa.findByEmailOptional(anyString()))
                .thenReturn(Optional.empty());

        when(usuarioRepositorioJpa.createUsuario(any(UsuarioEntidade.class)))
                .thenReturn(99999);
    }

    private void verificarConversaoEGravacao() throws Exception {
        verify(cursoRepositorioJpa).findBySigla("CURSO_ID_TESTE");

        verify(usuarioRepositorioJpa).createUsuario(
                any(UsuarioEntidade.class)
        );
    }
}