package com.cefet.godziny.infraestrutura.rest.atividade;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import com.cefet.godziny.api.atividade.AtividadeAtualizarDto;
import com.cefet.godziny.api.atividade.AtividadeDto;
import com.cefet.godziny.api.atividade.AtividadeFiltroDto;
import com.cefet.godziny.api.atividade.AtividadeRecuperarDto;
import com.cefet.godziny.constantes.atividade.EnumStatus;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeEntidade;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.atividade.arquivo.ArquivoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.atividade.arquivo.ArquivoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaEntidade;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpa;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class AtividadeControleTest{
    private static final UUID ID = UUID.randomUUID();
    private static final CursoEntidade CURSO = new CursoEntidade(
        UUID.randomUUID(),
        "ENG_ELET_BH",
        "Engenharia Elétrica",
        500,
        new UsuarioEntidade(99999, null, "nome TESTE", "teste@test.com", "senha TESTE", EnumRecursos.ADM, LocalDateTime.now())
    );
    private static final UsuarioEntidade USUARIO = new UsuarioEntidade(
        99999,
        CURSO,
        "Usuario teste",
        "usuarioteste@gmail.com",
        "123456",
        EnumRecursos.NORMAL,
        LocalDateTime.now()
    );
    private static final CategoriaEntidade CATEGORIA = new CategoriaEntidade(
        UUID.randomUUID(),
        CURSO,
        "Categoria Teste",
        (float) 0.5,
        (float) 0.1,
        "Categoria Descrição Teste"
    );
    private static final ArquivoEntidade ARQUIVO = new ArquivoEntidade(
        UUID.randomUUID(),
        "exemplo.txt",
        "texto/plain",
        "Este é um exemplo de conteúdo de texto para o arquivo.".getBytes()
    );

    private AtividadeEntidade entidade;
    private AtividadeDto dto;

    @InjectMocks
    private AtividadeControle controler;

    @Mock
    private CategoriaRepositorioJpa categoriaRepositorioJpa;

    @Mock
    private UsuarioRepositorioJpa usuarioRepositorioJpa;

    @Mock
    private CursoRepositorioJpa cursoRepositorioJpa;

    @Mock
    private AtividadeRepositorioJpa atividadeRepositorioJpa;

    @Mock
    private ArquivoRepositorioJpa arquivoRepositorioJpa;
 
    @BeforeEach
    void inicializarDados() {
        MockitoAnnotations.openMocks(this);
        controler = new AtividadeControle(atividadeRepositorioJpa, usuarioRepositorioJpa,  cursoRepositorioJpa, categoriaRepositorioJpa, arquivoRepositorioJpa);
    };

    @AfterEach
    void limparDados() {
        this.entidade = null;
        this.dto = null;

        categoriaRepositorioJpa.deleteAll();
        arquivoRepositorioJpa.deleteAll();
        usuarioRepositorioJpa.deleteAll();
        atividadeRepositorioJpa.deleteAll();
    }

    @Test
    @DisplayName("Should search an Atividade by Id successfully")
    void testGetAtividadeSuccess() throws Exception {
        this.entidade = createAtividadeEntidade();
       
        when(atividadeRepositorioJpa.findById(Mockito.any(UUID.class))).thenReturn(entidade);
        ResponseEntity<AtividadeRecuperarDto> response = controler.getAtividade(ID);

        assertThat(response.getBody()).isInstanceOf(AtividadeRecuperarDto.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @SuppressWarnings({"unchecked"})
    @Test
    @DisplayName("Should search all Atividades successfully")
    void testPesquisarAtividadesSuccess() throws Exception {
        this.entidade = createAtividadeEntidade();
        AtividadeFiltroDto filtroDto = new AtividadeFiltroDto();
        Page<AtividadeEntidade> page = new PageImpl<>(List.of(entidade));
        Pageable pageable = PageRequest.of(0, 10);

        when(atividadeRepositorioJpa.listAtividades(Mockito.any(Specification.class), Mockito.any(Pageable.class))).thenReturn(page);
        ResponseEntity<Page<AtividadeRecuperarDto>> response = controler.pesquisarAtividades(pageable, filtroDto);

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSizeGreaterThan(0); 
        assertThat(response.getBody().getSize()).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Should create a new Atividade successfully")
    void testCreateAtividadeSuccess() throws Exception {
        this.dto = createAtividadeDto();
        MultipartFile multipartFile = new MockMultipartFile("arquivo.txt", "arquivo.txt", "text/plain", "conteúdo do arquivo".getBytes());

        when(arquivoRepositorioJpa.createArquivo(Mockito.any(MultipartFile.class), Mockito.anyString())).thenReturn(ARQUIVO);
        when(atividadeRepositorioJpa.createAtividade(Mockito.any(AtividadeEntidade.class))).thenReturn(ID);
        ResponseEntity<UUID> response = controler.createAtividade(dto, multipartFile);

        assertThat(response.getBody()).isInstanceOf(UUID.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("Should update an existing Atividade successfully")
    void testupdateAtividadeSuccess() throws Exception {
        this.entidade = createAtividadeEntidade();
        AtividadeAtualizarDto dtoAtualizar = new AtividadeAtualizarDto(ID, USUARIO.getMatricula(), CATEGORIA.getId(), "AAtividade atualizada", LocalDateTime.now(), EnumStatus.APROVADA, ARQUIVO.getId(), (float) 2.9, "Comentário teste");

        when(arquivoRepositorioJpa.findById(Mockito.any(UUID.class))).thenReturn(ARQUIVO);
        when(categoriaRepositorioJpa.findById(Mockito.any(UUID.class))).thenReturn(CATEGORIA);
        when(usuarioRepositorioJpa.findById(Mockito.anyInt())).thenReturn(USUARIO);
        when(atividadeRepositorioJpa.updateAtividade(Mockito.any(AtividadeEntidade.class))).thenReturn(ID);
        when(atividadeRepositorioJpa.findById(Mockito.any(UUID.class))).thenReturn(entidade);
        ResponseEntity<UUID> response = controler.updateAtividade(ID, dtoAtualizar);

        assertThat(response.getBody()).isInstanceOf(UUID.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Should delete an Atividade successfully")
    void testRemoveAtividadeSuccess() throws Exception {
        this.entidade = createAtividadeEntidade();
        entidade.setStatus(EnumStatus.REJEITADA);

        when(atividadeRepositorioJpa.findById(Mockito.any(UUID.class))).thenReturn(entidade);
        doNothing().when(arquivoRepositorioJpa).deleteArquivo(Mockito.any(UUID.class));
        ResponseEntity<Void> response = controler.removeAtividade(ID);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private AtividadeEntidade createAtividadeEntidade(){
        AtividadeEntidade atividade = new AtividadeEntidade(
            ID,
            USUARIO,
            CATEGORIA,
            "Atividade teste",
            LocalDateTime.now(),
            EnumStatus.SIMULANDO,
            ARQUIVO,
            (float) 2.0,
            "Comentário teste"
        );
        return atividade;
    }

    private AtividadeDto createAtividadeDto(){
        AtividadeDto atividade = new AtividadeDto();
        atividade.setId(ID);
        atividade.setUsuarioId(USUARIO.getMatricula());
        atividade.setCategoriaId(CATEGORIA.getId());
        atividade.setTitulo("Atividade teste");
        atividade.setCreatedAt(LocalDateTime.now());
        atividade.setStatus(EnumStatus.SIMULANDO);
        atividade.setArquivoId(ARQUIVO.getId());
        atividade.setCargaHoraria((float) 2.9);
        atividade.setComentario("Comentário teste");
        return atividade;
    }
}
