package com.cefet.godziny.infraestrutura.rest.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cefet.godziny.api.usuario.UsuarioDto;
import com.cefet.godziny.api.usuario.UsuarioFiltroDto;
import com.cefet.godziny.api.usuario.UsuarioRecuperarDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpa;

@ExtendWith(MockitoExtension.class)
class UsuarioControleTest {

    private static final Integer MATRICULA = 99999;
    private static final Integer MATRICULA_ADMIN = 88888;

    private static final String CURSO_ID = "ENG_ELET_BH";
    private static final String NOME = "Usuário para Testes";
    private static final String EMAIL = "usuarioteste@usuarioteste.com";
    private static final String SENHA = "teste123";

    private static final LocalDateTime DATA =
        LocalDateTime.of(2026, 1, 15, 10, 30);

    @Mock
    private CursoRepositorioJpa cursoRepositorioJpa;

    @Mock
    private UsuarioRepositorioJpa usuarioRepositorioJpa;

    @Mock
    private AtividadeRepositorioJpa atividadeRepositorioJpa;

    @Mock
    private PasswordEncoder enconder;

    @InjectMocks
    private UsuarioControle controller;

    private UsuarioEntidade administrador;
    private UsuarioEntidade entidade;
    private CursoEntidade cursoEntidade;
    private UsuarioDto dto;

    @BeforeEach
    void inicializarDados() {
        administrador = criarAdministrador();
        configurarAutenticacao(administrador);

        cursoEntidade = criarCursoEntidade();
        entidade = criarUsuarioEntidade();
        dto = criarUsuarioDto();
    }

    @AfterEach
    void limparDados() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should retrieve a user by registration number successfully")
    void testGetUsuarioSuccess() throws Exception {
        when(usuarioRepositorioJpa.findById(MATRICULA))
            .thenReturn(entidade);

        ResponseEntity<UsuarioRecuperarDto> response =
            controller.getUsuario(MATRICULA);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMatricula()).isEqualTo(MATRICULA);
        assertThat(response.getBody().getNome()).isEqualTo(NOME);
    }

    @Test
    @DisplayName("Should retrieve a user without a course successfully")
    void testGetUsuarioWithoutCursoSuccess() throws Exception {
        entidade.setCurso(null);

        when(usuarioRepositorioJpa.findById(MATRICULA))
            .thenReturn(entidade);

        ResponseEntity<UsuarioRecuperarDto> response =
            controller.getUsuario(MATRICULA);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMatricula()).isEqualTo(MATRICULA);
        assertThat(response.getBody().getCurso()).isNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Should search users successfully")
    void testPesquisarUsuariosSuccess() throws Exception {
        UsuarioFiltroDto filtroDto = new UsuarioFiltroDto();

        Page<UsuarioEntidade> page =
            new PageImpl<>(List.of(entidade));

        Pageable pageable = PageRequest.of(0, 10);

        when(cursoRepositorioJpa.findByCoordenador(MATRICULA_ADMIN))
            .thenReturn(List.of(cursoEntidade.getId()));

        when(usuarioRepositorioJpa.listUsuarios(
            any(Specification.class),
            eq(pageable)
        )).thenReturn(page);

        ResponseEntity<Page<UsuarioRecuperarDto>> response =
            controller.pesquisarUsuarios(pageable, filtroDto);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getMatricula())
            .isEqualTo(MATRICULA);
    }

    @Test
    @DisplayName("Should create a new user successfully")
    void testCreateUsuarioSuccess() throws Exception {
        when(cursoRepositorioJpa.findBySigla(CURSO_ID))
            .thenReturn(cursoEntidade);

        when(enconder.encode(SENHA)).thenReturn("senha_criptografada");

        when(usuarioRepositorioJpa.createUsuario(any(UsuarioEntidade.class)))
            .thenReturn(MATRICULA);

        ResponseEntity<Integer> response =
            controller.createUsuario(dto);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(MATRICULA);
    }

    @Test
    @DisplayName("Should update an existing user successfully")
    void testUpdateUsuarioSuccess() throws Exception {
        dto.setMatricula(MATRICULA);
        dto.setNome("Usuário Teste Atualizado");

        when(cursoRepositorioJpa.findBySigla(CURSO_ID))
            .thenReturn(cursoEntidade);

        when(usuarioRepositorioJpa.findById(anyInt()))
            .thenReturn(entidade);

        when(enconder.encode(SENHA)).thenReturn("senha_criptografada");

        when(usuarioRepositorioJpa.updateUsuario(any(UsuarioEntidade.class)))
            .thenReturn(MATRICULA);

        ResponseEntity<Integer> response =
            controller.updateUsuario(MATRICULA, dto);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(MATRICULA);
    }

    @Test
    @DisplayName("Should remove a user successfully")
    void testRemoveUsuarioSuccess() throws Exception {
        when(usuarioRepositorioJpa.findById(MATRICULA))
            .thenReturn(entidade);

        ResponseEntity<Void> response =
            controller.removeUsuario(MATRICULA);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }

    private UsuarioEntidade criarAdministrador() {
        return new UsuarioEntidade(
            MATRICULA_ADMIN,
            null,
            "Administrador Teste",
            "admin@teste.com",
            "senha_admin",
            EnumRecursos.ADM,
            DATA
        );
    }

    private CursoEntidade criarCursoEntidade() {
        return new CursoEntidade(
            UUID.randomUUID(),
            CURSO_ID,
            "Engenharia Elétrica",
            500,
            administrador
        );
    }

    private UsuarioEntidade criarUsuarioEntidade() {
        return new UsuarioEntidade(
            MATRICULA,
            cursoEntidade,
            NOME,
            EMAIL,
            SENHA,
            EnumRecursos.NORMAL,
            DATA
        );
    }

    private UsuarioDto criarUsuarioDto() {
        UsuarioDto usuario = new UsuarioDto();

        usuario.setMatricula(null);
        usuario.setCursoSigla(CURSO_ID);
        usuario.setNome(NOME);
        usuario.setEmail(EMAIL);
        usuario.setSenha(SENHA);
        usuario.setTipo(EnumRecursos.NORMAL);
        usuario.setCreatedAt(DATA);

        return usuario;
    }

    private void configurarAutenticacao(UsuarioEntidade usuario) {
        var authentication =
            new UsernamePasswordAuthenticationToken(usuario, null);

        SecurityContextHolder.getContext()
            .setAuthentication(authentication);
    }
}