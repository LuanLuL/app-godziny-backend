package com.cefet.godziny.domain.casouso.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.cefet.godziny.api.usuario.UsuarioRecuperarDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpa;
import com.cefet.godziny.infraestrutura.rest.curso.CursoRestConverter;

@ExtendWith(MockitoExtension.class)
class ListarUsuarioCasoUsoTest {

    private static final Integer MATRICULA_ADMIN = 1;
    private static final Integer MATRICULA_USUARIO = 2;

    private static final String NOME_USUARIO = "NOME_TESTE";
    private static final String EMAIL_USUARIO = "usuario@teste.com";
    private static final String SENHA_USUARIO = "senha_teste";
    private static final String SIGLA_CURSO = "SIGLA_TESTE";
    private static final String NOME_CURSO = "CURSO_TESTE";

    @Mock
    private UsuarioRepositorioJpa usuarioRepositorioJpa;

    @InjectMocks
    private ListarUsuarioCasoUso listarUsuarioCasoUso;

    private UsuarioEntidade administrador;
    private UsuarioEntidade usuarioEntidade;
    private CursoEntidade cursoEntidade;
    private LocalDateTime createdAt;

    @BeforeEach
    void inicializarDados() {
        createdAt = LocalDateTime.of(2026, 1, 15, 10, 30);

        administrador = criarUsuarioAdministrador();
        configurarAutenticacao(administrador);

        cursoEntidade = criarCursoEntidade();
        usuarioEntidade = criarUsuarioEntidade();

        listarUsuarioCasoUso = new ListarUsuarioCasoUso(
            usuarioRepositorioJpa,
            MATRICULA_USUARIO
        );
    }

    @AfterEach
    void limparDados() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should list a user successfully")
    void deveListarUsuarioComSucesso() throws Exception {
        when(usuarioRepositorioJpa.findById(MATRICULA_USUARIO))
            .thenReturn(usuarioEntidade);

        UsuarioRecuperarDto expectedDto = criarUsuarioRecuperarDtoEsperado();

        UsuarioRecuperarDto result =
            listarUsuarioCasoUso.validarListagem();

        assertThat(result)
            .isNotNull()
            .usingRecursiveComparison()
            .isEqualTo(expectedDto);

        verify(usuarioRepositorioJpa).findById(MATRICULA_USUARIO);
    }

    @Test
    @DisplayName("Should throw an exception when the user is not an administrator")
    void deveLancarExcecaoQuandoUsuarioNaoForAdministrador() {
        EnumRecursos tipoNaoAdministrador = obterTipoNaoAdministrador();

        UsuarioEntidade usuarioNaoAdministrador =
            org.mockito.Mockito.mock(UsuarioEntidade.class);

        when(usuarioNaoAdministrador.getTipo())
            .thenReturn(tipoNaoAdministrador);

        configurarAutenticacao(usuarioNaoAdministrador);

        assertThatThrownBy(() -> listarUsuarioCasoUso.validarListagem())
            .isInstanceOf(UsuarioNaoAutorizadoException.class);

        verifyNoInteractions(usuarioRepositorioJpa);
    }

    @Test
    @DisplayName("Should throw a NullPointerException when the user is not found")
    void deveLancarExcecaoQuandoUsuarioNaoForEncontrado() throws Exception {
        when(usuarioRepositorioJpa.findById(MATRICULA_USUARIO))
            .thenReturn(null);


        assertThatThrownBy(() -> listarUsuarioCasoUso.validarListagem())
            .isInstanceOf(NullPointerException.class);

        verify(usuarioRepositorioJpa).findById(MATRICULA_USUARIO);
    }

    @Test
    @DisplayName("Should search for the user using the provided registration number")
    void deveBuscarUsuarioPelaMatriculaInformada() throws Exception {
        when(usuarioRepositorioJpa.findById(MATRICULA_USUARIO))
            .thenReturn(usuarioEntidade);

        listarUsuarioCasoUso.validarListagem();

        verify(usuarioRepositorioJpa).findById(MATRICULA_USUARIO);
    }

    private UsuarioEntidade criarUsuarioAdministrador() {
        return new UsuarioEntidade(
            MATRICULA_ADMIN,
            null,
            "ADMINISTRADOR",
            "admin@teste.com",
            "senha_admin",
            EnumRecursos.ADM,
            createdAt
        );
    }

    private CursoEntidade criarCursoEntidade() {
        return new CursoEntidade(
            UUID.randomUUID(),
            SIGLA_CURSO,
            NOME_CURSO,
            1,
            administrador
        );
    }

    private UsuarioEntidade criarUsuarioEntidade() {
        return new UsuarioEntidade(
            MATRICULA_USUARIO,
            cursoEntidade,
            NOME_USUARIO,
            EMAIL_USUARIO,
            SENHA_USUARIO,
            obterTipoNaoAdministrador(),
            createdAt
        );
    }

    private UsuarioRecuperarDto criarUsuarioRecuperarDtoEsperado() {
        UsuarioRecuperarDto dto = new UsuarioRecuperarDto();

        dto.setMatricula(usuarioEntidade.getMatricula());
        dto.setCurso(
            CursoRestConverter.EntidadeToCursoRecuperarDto(
                usuarioEntidade.getCurso()
            )
        );
        dto.setNome(usuarioEntidade.getNome());
        dto.setEmail(usuarioEntidade.getEmail());
        dto.setSenha(usuarioEntidade.getSenha());
        dto.setTipo(usuarioEntidade.getTipo());
        dto.setCreatedAt(usuarioEntidade.getCreatedAt());

        return dto;
    }

    private EnumRecursos obterTipoNaoAdministrador() {
        return java.util.Arrays.stream(EnumRecursos.values())
            .filter(tipo -> tipo != EnumRecursos.ADM)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "EnumRecursos must contain a value other than ADM"
            ));
    }

    private void configurarAutenticacao(UsuarioEntidade usuario) {
        var authentication =
            new UsernamePasswordAuthenticationToken(usuario, null);

        SecurityContextHolder.getContext()
            .setAuthentication(authentication);
    }
}