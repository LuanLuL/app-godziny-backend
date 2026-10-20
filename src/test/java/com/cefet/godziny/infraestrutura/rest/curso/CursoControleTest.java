package com.cefet.godziny.infraestrutura.rest.curso;

import com.cefet.godziny.api.curso.CursoDto;
import com.cefet.godziny.api.curso.CursoFiltroDto;
import com.cefet.godziny.api.curso.CursoRecuperarDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CursoControleTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String SIGLA = "ENG_ELET_BH";
    private static final Integer CARGA_HORARIA_COMPLEMENTAR = 500;
    private static final String NOME = "Engenharia Elétrica";
    private static final Integer MATRICULA_ADMIN = 99999;

    @Mock
    private CursoRepositorioJpa cursoRepositorioJpa;

    @Mock
    private UsuarioRepositorioJpa usuarioRepositorioJpa;

    @Mock
    private CategoriaRepositorioJpa categoriaRepositorioJpa;

    private CursoControle controller;

    private UsuarioEntidade coordenador;
    private CursoEntidade entidade;
    private CursoDto dto;

    @BeforeEach
    void inicializarDados() {
        coordenador = criarAdministrador();
        entidade = criarCursoEntidade();
        dto = createCursoDto();

        controller = new CursoControle(
                cursoRepositorioJpa,
                usuarioRepositorioJpa,
                categoriaRepositorioJpa
        );

        autenticarAdministrador();
    }

    @AfterEach
    void limparDados() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarAdministrador() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        coordenador,
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
                "admin@teste.com",
                "senha123",
                EnumRecursos.ADM,
                LocalDateTime.now()
        );
    }

    private CursoEntidade criarCursoEntidade() {
        return new CursoEntidade(
                ID,
                SIGLA,
                NOME,
                CARGA_HORARIA_COMPLEMENTAR,
                coordenador
        );
    }

    private CursoDto createCursoDto() {
        CursoDto curso = new CursoDto();
        curso.setId(ID);
        curso.setSigla(SIGLA);
        curso.setNome(NOME);
        curso.setCarga_horaria_complementar(
                CARGA_HORARIA_COMPLEMENTAR
        );
        curso.setCoordenador_matricula(
                coordenador.getMatricula()
        );

        return curso;
    }

    @Test
    @DisplayName("Should retrieve a course by acronym successfully")
    void testGetCursoSuccess() throws Exception {
        when(cursoRepositorioJpa.findBySigla(SIGLA))
                .thenReturn(entidade);

        ResponseEntity<CursoRecuperarDto> response =
                controller.getCurso(SIGLA);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSigla())
                .isEqualTo(SIGLA);
        assertThat(response.getBody().getNome())
                .isEqualTo(NOME);

        verify(cursoRepositorioJpa).findBySigla(SIGLA);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Should search courses successfully")
    void testPesquisarCursosSuccess() throws Exception {
        CursoFiltroDto filtroDto = new CursoFiltroDto();
        filtroDto.setSigla(SIGLA);
        filtroDto.setNome(NOME);

        Pageable pageable = PageRequest.of(0, 10);

        Page<CursoEntidade> page = new PageImpl<>(
                List.of(entidade),
                pageable,
                1
        );

        when(cursoRepositorioJpa.listCursos(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(page);

        ResponseEntity<Page<CursoRecuperarDto>> response =
                controller.pesquisarCursos(pageable, filtroDto);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getSigla())
                .isEqualTo(SIGLA);

        verify(cursoRepositorioJpa).listCursos(
                any(Specification.class),
                eq(pageable)
        );
    }

    @Test
    @DisplayName("Should create a course successfully")
    void testCreateCursoSuccess() throws Exception {
        when(cursoRepositorioJpa.createCurso(any(CursoEntidade.class)))
                .thenReturn(SIGLA);

        when(usuarioRepositorioJpa.findById(anyInt()))
                .thenReturn(coordenador);

        ResponseEntity<String> response =
                controller.createCurso(dto);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(SIGLA);

        verify(usuarioRepositorioJpa)
                .findById(MATRICULA_ADMIN);

        verify(cursoRepositorioJpa)
                .createCurso(any(CursoEntidade.class));
    }

    @Test
    @DisplayName("Should update an existing course successfully")
    void testUpdateCursoSuccess() throws Exception {
        dto.setNome("Engenharia Elétrica Atualizada");

        when(cursoRepositorioJpa.findBySigla(SIGLA))
                .thenReturn(entidade);

        when(usuarioRepositorioJpa.findById(anyInt()))
                .thenReturn(coordenador);

        when(cursoRepositorioJpa.updateCurso(
                eq(SIGLA),
                any(CursoEntidade.class)
        )).thenReturn(SIGLA);

        ResponseEntity<String> response =
                controller.updateCurso(SIGLA, dto);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(SIGLA);

        verify(cursoRepositorioJpa).findBySigla(SIGLA);

        verify(cursoRepositorioJpa).updateCurso(
                eq(SIGLA),
                any(CursoEntidade.class)
        );
    }

    @Test
    @DisplayName("Should remove a course successfully")
    void testRemoveCursoSuccess() throws Exception {
        Page<UsuarioEntidade> pageUsers = new PageImpl<>(
                Collections.emptyList()
        );

        when(cursoRepositorioJpa.findBySigla(SIGLA))
                .thenReturn(entidade);

        when(usuarioRepositorioJpa.listUsuariosByCurso(
                any(Pageable.class),
                any(CursoEntidade.class)
        )).thenReturn(pageUsers);

        ResponseEntity<Void> response =
                controller.removeCurso(SIGLA);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();

        verify(cursoRepositorioJpa).findBySigla(SIGLA);

        verify(usuarioRepositorioJpa).listUsuariosByCurso(
                any(Pageable.class),
                eq(entidade)
        );
    }
}