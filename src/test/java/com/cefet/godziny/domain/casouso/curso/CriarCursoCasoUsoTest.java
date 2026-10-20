package com.cefet.godziny.domain.casouso.curso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.cefet.godziny.api.curso.CursoDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.CampoRepetidoNoBancoException;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.exceptions.curso.CriarCursoComUsuarioNormalException;
import com.cefet.godziny.infraestrutura.exceptions.curso.CriarCursoIncompletoException;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpa;

@ExtendWith(MockitoExtension.class)
class CriarCursoCasoUsoTest {

    private static final UUID CURSO_ID = UUID.randomUUID();
    private static final String SIGLA_CURSO = "TESTE";
    private static final String NOME_CURSO = "Curso de Teste";
    private static final int CARGA_HORARIA = 100;
    private static final int MATRICULA_COORDENADOR = 1;
    private static final int MATRICULA_ADMIN = 99999;

    @Mock
    private CursoRepositorioJpa cursoRepositorioJpa;

    @Mock
    private UsuarioRepositorioJpa usuarioRepositorioJpa;

    private CriarCursoCasoUso criarCursoCasoUso;
    private UsuarioEntidade administrador;

    @BeforeEach
    void setUp() {
        administrador = criarUsuario(MATRICULA_ADMIN, EnumRecursos.ADM);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                administrador,
                null,
                Collections.emptyList()
            )
        );

        criarCursoCasoUso = new CriarCursoCasoUso(
            cursoRepositorioJpa,
            usuarioRepositorioJpa,
            SIGLA_CURSO,
            NOME_CURSO,
            CARGA_HORARIA,
            MATRICULA_COORDENADOR
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should validate and create a course successfully")
    void shouldValidateAndCreateCourseSuccessfully() throws Exception {
        UsuarioEntidade coordenador = criarUsuario(
            MATRICULA_COORDENADOR,
            EnumRecursos.ADM
        );

        CursoDto dto = criarCursoDto();

        when(cursoRepositorioJpa.findBySiglaOptional(SIGLA_CURSO))
            .thenReturn(Optional.empty());

        when(usuarioRepositorioJpa.findById(MATRICULA_COORDENADOR))
            .thenReturn(coordenador);

        when(cursoRepositorioJpa.createCurso(any(CursoEntidade.class)))
            .thenReturn(SIGLA_CURSO);

        UsuarioEntidade coordenadorValidado =
            criarCursoCasoUso.validarCriacao();

        String response = criarCursoCasoUso.createCurso(
            dto,
            coordenadorValidado
        );

        assertThat(coordenadorValidado).isSameAs(coordenador);
        assertThat(response).isEqualTo(SIGLA_CURSO);

        verify(cursoRepositorioJpa).findBySiglaOptional(SIGLA_CURSO);
        verify(usuarioRepositorioJpa).findById(MATRICULA_COORDENADOR);
        verify(cursoRepositorioJpa).createCurso(any(CursoEntidade.class));
    }

    @Test
    @DisplayName("Should throw an exception when the course acronym is too long")
    void shouldThrowExceptionWhenCourseAcronymIsTooLong() throws Exception {
        criarCursoCasoUso.setSigla(
            "SIGLA_MAIOR_QUE_VINTE_CARACTERES"
        );

        CriarCursoIncompletoException exception = assertThrows(
            CriarCursoIncompletoException.class,
            () -> criarCursoCasoUso.validarCriacao()
        );

        assertThat(exception.getMessage())
            .isEqualTo("A sigla do curso deve ter entre 3 e 20 caracteres");

        verify(cursoRepositorioJpa, never()).findBySiglaOptional(anyString());
        verify(usuarioRepositorioJpa, never()).findById(anyInt());
    }

    @Test
    @DisplayName("Should throw an exception when the course acronym is too short")
    void shouldThrowExceptionWhenCourseAcronymIsTooShort() throws Exception {
        criarCursoCasoUso.setSigla("S");

        CriarCursoIncompletoException exception = assertThrows(
            CriarCursoIncompletoException.class,
            () -> criarCursoCasoUso.validarCriacao()
        );

        assertThat(exception.getMessage())
            .isEqualTo("A sigla do curso deve ter entre 3 e 20 caracteres");

        verify(cursoRepositorioJpa, never()).findBySiglaOptional(anyString());
        verify(usuarioRepositorioJpa, never()).findById(anyInt());
    }

    @Test
    @DisplayName("Should throw an exception when the course name is too long")
    void shouldThrowExceptionWhenCourseNameIsTooLong() throws Exception {
        criarCursoCasoUso.setNome(
            "This course name is intentionally much longer than the maximum allowed length"
        );

        CriarCursoIncompletoException exception = assertThrows(
            CriarCursoIncompletoException.class,
            () -> criarCursoCasoUso.validarCriacao()
        );

        assertThat(exception.getMessage())
            .isEqualTo("O nome do curso deve ter entre 3 e 50 caracteres");

        verify(cursoRepositorioJpa, never()).findBySiglaOptional(anyString());
        verify(usuarioRepositorioJpa, never()).findById(anyInt());
    }

    @Test
    @DisplayName("Should throw an exception when the course name is too short")
    void shouldThrowExceptionWhenCourseNameIsTooShort() throws Exception {
        criarCursoCasoUso.setNome("N");

        CriarCursoIncompletoException exception = assertThrows(
            CriarCursoIncompletoException.class,
            () -> criarCursoCasoUso.validarCriacao()
        );

        assertThat(exception.getMessage())
            .isEqualTo("O nome do curso deve ter entre 3 e 50 caracteres");

        verify(cursoRepositorioJpa, never()).findBySiglaOptional(anyString());
        verify(usuarioRepositorioJpa, never()).findById(anyInt());
    }

    @Test
    @DisplayName("Should throw an exception when complementary hours exceed the maximum")
    void shouldThrowExceptionWhenComplementaryHoursExceedMaximum() throws Exception {
        criarCursoCasoUso.setCargaHorariaComplementar(1000);

        CriarCursoIncompletoException exception = assertThrows(
            CriarCursoIncompletoException.class,
            () -> criarCursoCasoUso.validarCriacao()
        );

        assertThat(exception.getMessage())
            .isEqualTo(
                "A carga de horas complementares do curso deve estar entre 100 e 800"
            );

        verify(cursoRepositorioJpa, never()).findBySiglaOptional(anyString());
        verify(usuarioRepositorioJpa, never()).findById(anyInt());
    }

    @Test
    @DisplayName("Should throw an exception when complementary hours are below the minimum")
    void shouldThrowExceptionWhenComplementaryHoursAreBelowMinimum() throws Exception {
        criarCursoCasoUso.setCargaHorariaComplementar(50);

        CriarCursoIncompletoException exception = assertThrows(
            CriarCursoIncompletoException.class,
            () -> criarCursoCasoUso.validarCriacao()
        );

        assertThat(exception.getMessage())
            .isEqualTo(
                "A carga de horas complementares do curso deve estar entre 100 e 800"
            );

        verify(cursoRepositorioJpa, never()).findBySiglaOptional(anyString());
        verify(usuarioRepositorioJpa, never()).findById(anyInt());
    }

    @Test
    @DisplayName("Should throw an exception when the course acronym already exists")
    void shouldThrowExceptionWhenCourseAcronymAlreadyExists() throws Exception {
        CursoEntidade cursoExistente = new CursoEntidade(
            UUID.randomUUID(),
            SIGLA_CURSO,
            "Engenharia Eletrica",
            500,
            administrador
        );

        when(cursoRepositorioJpa.findBySiglaOptional(SIGLA_CURSO))
            .thenReturn(Optional.of(cursoExistente));

        CampoRepetidoNoBancoException exception = assertThrows(
            CampoRepetidoNoBancoException.class,
            () -> criarCursoCasoUso.validarCriacao()
        );

        assertThat(exception.getMessage())
            .isEqualTo(
                "Já existe um Curso com essa sigla cadastrado na base de dados"
            );

        verify(cursoRepositorioJpa).findBySiglaOptional(SIGLA_CURSO);
        verify(usuarioRepositorioJpa, never()).findById(anyInt());
    }

    @Test
    @DisplayName("Should throw an exception when the coordinator is a normal user")
    void shouldThrowExceptionWhenCoordinatorIsNormalUser() throws Exception {
        UsuarioEntidade coordenador = criarUsuario(
            MATRICULA_COORDENADOR,
            EnumRecursos.NORMAL
        );

        when(cursoRepositorioJpa.findBySiglaOptional(SIGLA_CURSO))
            .thenReturn(Optional.empty());

        when(usuarioRepositorioJpa.findById(MATRICULA_COORDENADOR))
            .thenReturn(coordenador);

        CriarCursoComUsuarioNormalException exception = assertThrows(
            CriarCursoComUsuarioNormalException.class,
            () -> criarCursoCasoUso.validarCriacao()
        );

        assertThat(exception.getMessage())
            .isEqualTo("O coordenador do curso deve ser um usuário do tipo 'ADM'");

        verify(cursoRepositorioJpa).findBySiglaOptional(SIGLA_CURSO);
        verify(usuarioRepositorioJpa).findById(MATRICULA_COORDENADOR);
    }

    @Test
    @DisplayName("Should reject course creation when the authenticated user is not an administrator")
    void shouldRejectCourseCreationWhenAuthenticatedUserIsNotAdministrator() throws Exception {
        UsuarioEntidade usuarioNormal = criarUsuario(
            MATRICULA_ADMIN,
            EnumRecursos.NORMAL
        );

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                usuarioNormal,
                null,
                Collections.emptyList()
            )
        );

        assertThrows(
            UsuarioNaoAutorizadoException.class,
            () -> criarCursoCasoUso.validarCriacao()
        );

        verify(cursoRepositorioJpa, never()).findBySiglaOptional(anyString());
        verify(usuarioRepositorioJpa, never()).findById(anyInt());
    }

    @Test
    @DisplayName("Should create a course and return its acronym")
    void shouldCreateCourseAndReturnItsAcronym() throws Exception {
        UsuarioEntidade coordenador = criarUsuario(
            MATRICULA_COORDENADOR,
            EnumRecursos.ADM
        );

        CursoDto dto = criarCursoDto();

        when(cursoRepositorioJpa.createCurso(any(CursoEntidade.class)))
            .thenReturn(SIGLA_CURSO);

        String response = criarCursoCasoUso.createCurso(dto, coordenador);

        assertThat(response).isEqualTo(SIGLA_CURSO);

        verify(cursoRepositorioJpa).createCurso(any(CursoEntidade.class));
    }

    private CursoDto criarCursoDto() {
        return new CursoDto(
            CURSO_ID,
            SIGLA_CURSO,
            NOME_CURSO,
            CARGA_HORARIA,
            MATRICULA_COORDENADOR
        );
    }

    private UsuarioEntidade criarUsuario(
        Integer matricula,
        EnumRecursos tipo
    ) {
        return new UsuarioEntidade(
            matricula,
            null,
            "Nome de Teste",
            "teste@test.com",
            "senhaTeste",
            tipo,
            LocalDateTime.now()
        );
    }
}