package com.cefet.godziny.domain.casouso.curso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
class AtualizarCursoCasoUsoTest {

    private static final UUID CURSO_ID = UUID.randomUUID();
    private static final String SIGLA_CURSO = "TESTE";
    private static final String SIGLA_ANTIGA = "TESTE_ANTIGO";
    private static final String NOME_CURSO = "Curso de Teste";
    private static final int CARGA_HORARIA = 100;
    private static final int MATRICULA_COORDENADOR = 1;
    private static final int MATRICULA_ADMIN = 99999;

    @Mock
    private CursoRepositorioJpa cursoRepositorioJpa;

    @Mock
    private UsuarioRepositorioJpa usuarioRepositorioJpa;

    private AtualizarCursoCasoUso atualizarCursoCasoUso;
    private UsuarioEntidade administrador;

    @BeforeEach
    void setUp() {
        administrador = criarUsuario(MATRICULA_ADMIN, EnumRecursos.ADM);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                administrador,
                null,
                java.util.Collections.emptyList()
            )
        );

        atualizarCursoCasoUso = new AtualizarCursoCasoUso(
            cursoRepositorioJpa,
            usuarioRepositorioJpa,
            CURSO_ID,
            SIGLA_CURSO,
            SIGLA_ANTIGA,
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
    @DisplayName("Should validate and update a course successfully")
    void shouldValidateAndUpdateCourseSuccessfully() throws Exception {
        UsuarioEntidade coordenador = criarUsuario(
            MATRICULA_COORDENADOR,
            EnumRecursos.ADM
        );

        CursoDto dto = new CursoDto(
            CURSO_ID,
            SIGLA_CURSO,
            NOME_CURSO,
            CARGA_HORARIA,
            MATRICULA_COORDENADOR
        );

        when(cursoRepositorioJpa.findBySigla(SIGLA_ANTIGA))
            .thenReturn(null);

        when(usuarioRepositorioJpa.findById(MATRICULA_COORDENADOR))
            .thenReturn(coordenador);

        when(cursoRepositorioJpa.updateCurso(
            eq(SIGLA_CURSO),
            any(CursoEntidade.class)
        )).thenReturn(SIGLA_CURSO);

        UsuarioEntidade coordenadorValidado =
            atualizarCursoCasoUso.validarAtualizacao();

        String response = atualizarCursoCasoUso.AtualizarCurso(
            SIGLA_CURSO,
            dto,
            coordenadorValidado
        );

        assertThat(coordenadorValidado).isSameAs(coordenador);
        assertThat(response).isEqualTo(SIGLA_CURSO);

        verify(cursoRepositorioJpa).findBySigla(SIGLA_ANTIGA);
        verify(usuarioRepositorioJpa).findById(MATRICULA_COORDENADOR);
        verify(cursoRepositorioJpa).updateCurso(
            eq(SIGLA_CURSO),
            any(CursoEntidade.class)
        );
    }

    @Test
    @DisplayName("Should throw an exception when the course acronym is too long")
    void shouldThrowExceptionWhenCourseAcronymIsTooLong() throws Exception{
        atualizarCursoCasoUso.setSigla(
            "SIGLA_MAIOR_QUE_VINTE_CARACTERES"
        );

        CriarCursoIncompletoException exception = assertThrows(
            CriarCursoIncompletoException.class,
            () -> atualizarCursoCasoUso.validarAtualizacao()
        );

        assertThat(exception.getMessage())
            .isEqualTo("A sigla do curso deve ter entre 3 e 20 caracteres");

        verify(cursoRepositorioJpa, never()).findBySigla(anyString());
        verify(usuarioRepositorioJpa, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw an exception when the course acronym is too short")
    void shouldThrowExceptionWhenCourseAcronymIsTooShort() throws Exception {
        atualizarCursoCasoUso.setSigla("S");

        CriarCursoIncompletoException exception = assertThrows(
            CriarCursoIncompletoException.class,
            () -> atualizarCursoCasoUso.validarAtualizacao()
        );

        assertThat(exception.getMessage())
            .isEqualTo("A sigla do curso deve ter entre 3 e 20 caracteres");

        verify(cursoRepositorioJpa, never()).findBySigla(anyString());
        verify(usuarioRepositorioJpa, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw an exception when the course name is too long")
    void shouldThrowExceptionWhenCourseNameIsTooLong() throws Exception {
        atualizarCursoCasoUso.setNome(
            "This course name is intentionally much longer than the maximum allowed length"
        );

        CriarCursoIncompletoException exception = assertThrows(
            CriarCursoIncompletoException.class,
            () -> atualizarCursoCasoUso.validarAtualizacao()
        );

        assertThat(exception.getMessage())
            .isEqualTo("O nome do curso deve ter entre 3 e 50 caracteres");

        verify(cursoRepositorioJpa, never()).findBySigla(anyString());
        verify(usuarioRepositorioJpa, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw an exception when the course name is too short")
    void shouldThrowExceptionWhenCourseNameIsTooShort() throws Exception {
        atualizarCursoCasoUso.setNome("N");

        CriarCursoIncompletoException exception = assertThrows(
            CriarCursoIncompletoException.class,
            () -> atualizarCursoCasoUso.validarAtualizacao()
        );

        assertThat(exception.getMessage())
            .isEqualTo("O nome do curso deve ter entre 3 e 50 caracteres");

        verify(cursoRepositorioJpa, never()).findBySigla(anyString());
        verify(usuarioRepositorioJpa, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw an exception when complementary hours exceed the maximum")
    void shouldThrowExceptionWhenComplementaryHoursExceedMaximum() throws Exception {
        atualizarCursoCasoUso.setCargaHorariaComplementar(1000);

        CriarCursoIncompletoException exception = assertThrows(
            CriarCursoIncompletoException.class,
            () -> atualizarCursoCasoUso.validarAtualizacao()
        );

        assertThat(exception.getMessage())
            .isEqualTo(
                "A carga de horas complementares do curso deve estar entre 100 e 800"
            );

        verify(cursoRepositorioJpa, never()).findBySigla(anyString());
        verify(usuarioRepositorioJpa, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw an exception when complementary hours are below the minimum")
    void shouldThrowExceptionWhenComplementaryHoursAreBelowMinimum() throws Exception {
        atualizarCursoCasoUso.setCargaHorariaComplementar(50);

        CriarCursoIncompletoException exception = assertThrows(
            CriarCursoIncompletoException.class,
            () -> atualizarCursoCasoUso.validarAtualizacao()
        );

        assertThat(exception.getMessage())
            .isEqualTo(
                "A carga de horas complementares do curso deve estar entre 100 e 800"
            );

        verify(cursoRepositorioJpa, never()).findBySigla(anyString());
        verify(usuarioRepositorioJpa, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw an exception when the course acronym already belongs to another course")
    void shouldThrowExceptionWhenCourseAcronymAlreadyExists() throws Exception {
        UUID outroCursoId = UUID.randomUUID();

        CursoEntidade outroCurso = new CursoEntidade(
            outroCursoId,
            SIGLA_ANTIGA,
            "Engenharia Eletrica",
            500,
            administrador
        );

        when(cursoRepositorioJpa.findBySigla(SIGLA_ANTIGA))
            .thenReturn(outroCurso);

        CampoRepetidoNoBancoException exception = assertThrows(
            CampoRepetidoNoBancoException.class,
            () -> atualizarCursoCasoUso.validarAtualizacao()
        );

        assertThat(exception.getMessage())
            .isEqualTo(
                "Já existe um Curso com essa sigla cadastrado na base de dados"
            );

        verify(cursoRepositorioJpa).findBySigla(SIGLA_ANTIGA);
        verify(usuarioRepositorioJpa, never()).findById(any());
    }

    @Test
    @DisplayName("Should allow updating a course when its acronym belongs to the same course")
    void shouldAllowUpdatingCourseWhenAcronymBelongsToSameCourse() throws Exception {
        CursoEntidade mesmoCurso = new CursoEntidade(
            CURSO_ID,
            SIGLA_ANTIGA,
            NOME_CURSO,
            CARGA_HORARIA,
            administrador
        );

        UsuarioEntidade coordenador = criarUsuario(
            MATRICULA_COORDENADOR,
            EnumRecursos.ADM
        );

        when(cursoRepositorioJpa.findBySigla(SIGLA_ANTIGA))
            .thenReturn(mesmoCurso);

        when(usuarioRepositorioJpa.findById(MATRICULA_COORDENADOR))
            .thenReturn(coordenador);

        UsuarioEntidade coordenadorValidado =
            atualizarCursoCasoUso.validarAtualizacao();

        assertThat(coordenadorValidado).isSameAs(coordenador);
    }

    @Test
    @DisplayName("Should throw an exception when the coordinator is a normal user")
    void shouldThrowExceptionWhenCoordinatorIsNormalUser() throws Exception {
        UsuarioEntidade coordenador = criarUsuario(
            MATRICULA_COORDENADOR,
            EnumRecursos.NORMAL
        );

        when(cursoRepositorioJpa.findBySigla(SIGLA_ANTIGA))
            .thenReturn(null);

        when(usuarioRepositorioJpa.findById(MATRICULA_COORDENADOR))
            .thenReturn(coordenador);

        CriarCursoComUsuarioNormalException exception = assertThrows(
            CriarCursoComUsuarioNormalException.class,
            () -> atualizarCursoCasoUso.validarAtualizacao()
        );

        assertThat(exception.getMessage())
            .isEqualTo("O coordenador do curso deve ser um usuário do tipo 'ADM'");
    }

    @Test
    @DisplayName("Should reject course updates when the authenticated user is not an administrator")
    void shouldRejectCourseUpdateWhenAuthenticatedUserIsNotAdministrator() throws Exception {
        UsuarioEntidade usuarioNormal = criarUsuario(
            MATRICULA_ADMIN,
            EnumRecursos.NORMAL
        );

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                usuarioNormal,
                null,
                java.util.Collections.emptyList()
            )
        );

        assertThrows(
            UsuarioNaoAutorizadoException.class,
            () -> atualizarCursoCasoUso.validarAtualizacao()
        );

        verify(cursoRepositorioJpa, never()).findBySigla(anyString());
        verify(usuarioRepositorioJpa, never()).findById(any());
    }

    @Test
    @DisplayName("Should update the course and return the repository response")
    void shouldUpdateCourseAndReturnRepositoryResponse() throws Exception {
        UsuarioEntidade coordenador = criarUsuario(
            MATRICULA_COORDENADOR,
            EnumRecursos.ADM
        );

        CursoDto dto = new CursoDto(
            CURSO_ID,
            SIGLA_CURSO,
            NOME_CURSO,
            CARGA_HORARIA,
            MATRICULA_COORDENADOR
        );

        when(cursoRepositorioJpa.updateCurso(
            eq(SIGLA_CURSO),
            any(CursoEntidade.class)
        )).thenReturn(SIGLA_CURSO);

        String response = atualizarCursoCasoUso.AtualizarCurso(
            SIGLA_CURSO,
            dto,
            coordenador
        );

        assertThat(response).isEqualTo(SIGLA_CURSO);

        verify(cursoRepositorioJpa).updateCurso(
            eq(SIGLA_CURSO),
            any(CursoEntidade.class)
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