package com.cefet.godziny.infraestrutura.rest.categoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

import com.cefet.godziny.api.categoria.CategoriaDto;
import com.cefet.godziny.api.categoria.CategoriaFiltroDto;
import com.cefet.godziny.api.categoria.CategoriaRecuperarDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaEntidade;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;

@ExtendWith(MockitoExtension.class)
class CategoriaControleTest {

    private static final UUID ID = UUID.randomUUID();

    private static final Integer MATRICULA_ADMIN = 99999;

    private static final String NOME = "Categoria Teste";
    private static final String DESCRICAO = "Categoria Descrição Teste";
    private static final String SIGLA_CURSO = "ENG_ELET_BH";
    private static final String NOME_CURSO = "Engenharia Elétrica";

    private static final float PORCENTAGEM_HORAS_MAXIMAS = 0.5f;
    private static final float HORAS_MULTIPLICADOR = 0.1f;

    @Mock
    private CategoriaRepositorioJpa categoriaRepositorioJpa;

    @Mock
    private CursoRepositorioJpa cursoRepositorioJpa;

    @Mock
    private AtividadeRepositorioJpa atividadeRepositorioJpa;

    @InjectMocks
    private CategoriaControle controller;

    private UsuarioEntidade administrador;
    private CursoEntidade cursoEntidade;
    private CategoriaEntidade categoriaEntidade;
    private CategoriaDto categoriaDto;

    @BeforeEach
    void inicializarDados() {
        administrador = criarAdministrador();
        configurarAutenticacao(administrador);

        cursoEntidade = criarCursoEntidade();
        categoriaEntidade = criarCategoriaEntidade();
        categoriaDto = criarCategoriaDto();
    }

    @AfterEach
    void limparDados() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should retrieve a category by ID successfully")
    void deveBuscarCategoriaPorIdComSucesso() throws Exception {
        when(categoriaRepositorioJpa.findById(ID))
            .thenReturn(categoriaEntidade);

        ResponseEntity<CategoriaRecuperarDto> response =
            controller.getCategoria(ID);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
            .isInstanceOf(CategoriaRecuperarDto.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Should search categories successfully")
    void devePesquisarCategoriasComSucesso() throws Exception {
        CategoriaFiltroDto filtroDto = new CategoriaFiltroDto();

        Page<CategoriaEntidade> page =
            new PageImpl<>(List.of(categoriaEntidade));

        Pageable pageable = PageRequest.of(0, 10);

        when(categoriaRepositorioJpa.listCategorias(
            org.mockito.ArgumentMatchers.any(Specification.class),
            org.mockito.ArgumentMatchers.eq(pageable)
        )).thenReturn(page);

        ResponseEntity<Page<CategoriaRecuperarDto>> response =
            controller.pesquisarCategorias(pageable, filtroDto);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should create a category successfully")
    void deveCriarCategoriaComSucesso() throws Exception {
        when(cursoRepositorioJpa.findBySigla(SIGLA_CURSO))
            .thenReturn(cursoEntidade);

        when(categoriaRepositorioJpa.createCategoria(
            org.mockito.ArgumentMatchers.any(CategoriaEntidade.class)
        )).thenReturn(ID);

        ResponseEntity<UUID> response =
            controller.createCategoria(categoriaDto);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(ID);
    }

    @Test
    @DisplayName("Should update an existing category successfully")
    void deveAtualizarCategoriaComSucesso() throws Exception {
        categoriaDto.setNome("Engenharia Elétrica Atualizada");

        when(cursoRepositorioJpa.findBySigla(SIGLA_CURSO))
            .thenReturn(cursoEntidade);

        when(categoriaRepositorioJpa.findById(ID))
            .thenReturn(categoriaEntidade);

        when(categoriaRepositorioJpa.updateCategoria(
            org.mockito.ArgumentMatchers.any(CategoriaEntidade.class)
        )).thenReturn(ID);

        ResponseEntity<UUID> response =
            controller.updateCategoria(ID, categoriaDto);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(ID);
    }

    @Test
    @DisplayName("Should remove a category successfully")
    void deveRemoverCategoriaComSucesso() throws Exception {
        when(categoriaRepositorioJpa.findById(ID))
            .thenReturn(categoriaEntidade);

        ResponseEntity<Void> response =
            controller.removeCategoria(ID);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }

    private UsuarioEntidade criarAdministrador() {
        return new UsuarioEntidade(
            MATRICULA_ADMIN,
            null,
            "Administrador Teste",
            "admin@test.com",
            "senha_teste",
            EnumRecursos.ADM,
            LocalDateTime.now()
        );
    }

    private CursoEntidade criarCursoEntidade() {
        return new CursoEntidade(
            UUID.randomUUID(),
            SIGLA_CURSO,
            NOME_CURSO,
            500,
            administrador
        );
    }

    private CategoriaEntidade criarCategoriaEntidade() {
        return new CategoriaEntidade(
            ID,
            cursoEntidade,
            NOME,
            PORCENTAGEM_HORAS_MAXIMAS,
            HORAS_MULTIPLICADOR,
            DESCRICAO
        );
    }

    private CategoriaDto criarCategoriaDto() {
        CategoriaDto dto = new CategoriaDto();

        dto.setId(ID);
        dto.setCursoSigla(SIGLA_CURSO);
        dto.setNome(NOME);
        dto.setPorcentagemHorasMaximas(PORCENTAGEM_HORAS_MAXIMAS);
        dto.setHorasMultiplicador(HORAS_MULTIPLICADOR);
        dto.setDescricao(DESCRICAO);

        return dto;
    }

    private void configurarAutenticacao(UsuarioEntidade usuario) {
        var authentication =
            new UsernamePasswordAuthenticationToken(usuario, null);

        SecurityContextHolder.getContext()
            .setAuthentication(authentication);
    }
}