package com.cefet.godziny.infraestrutura.persistencia.usuario;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.test.context.ActiveProfiles;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.usuario.UsuarioNaoEncontradoException;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class UsuarioRepositorioJpaTest {
    private static final  Integer MATRICULA = 9999999;
    private static final String NOME = "Teste";
    private static final CursoEntidade CURSO_ENTIDADE = new CursoEntidade(
        UUID.randomUUID(),
        "ODONT_DIV",
        "Odontologia",
        615,
        new UsuarioEntidade(99999, null, "nome TESTE", "teste@test.com", "senha TESTE", EnumRecursos.ADM, LocalDateTime.now())
    );
    private static final String EMAIL = "teste@teste.com";
    private static final String SENHA = "teste";
    private static final EnumRecursos TIPO =EnumRecursos.NORMAL;
    private static final LocalDateTime DATA = LocalDateTime.now();


    private Optional<UsuarioEntidade> optional;
    private UsuarioEntidade entidade;

    @Mock
    UsuarioRepositorioJpaSpring usuarioRepositorioJpaSpring;

    @InjectMocks
    UsuarioRepositorioJpa usuarioRepositorio;


    @BeforeEach
    void start() {
        MockitoAnnotations.openMocks(this);
        usuarioRepositorio = new UsuarioRepositorioJpa(usuarioRepositorioJpaSpring);
    };

    @AfterEach
    void clean() {
        this.optional = null;
        this.entidade = null;
    }

    @Test
    @DisplayName("Search for  an Usuario by MATRICULA and return an existing successfully from DataBase")
    void testFindByIdSuccess() throws Exception {
        this.optional = createOptionalUsuario();

        when(usuarioRepositorioJpaSpring.findById(Mockito.any())).thenReturn(this.optional);
        UsuarioEntidade result = usuarioRepositorio.findById(MATRICULA);

        assertThat(result).isInstanceOf(UsuarioEntidade.class);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Search for an Usuario by MATRICULA and return an excepiton because the MATRICULA doesn't exist")
    void testFindByIdUsuarioNaoEncontradoException() throws Exception {
        this.optional = Optional.empty();

        when(usuarioRepositorioJpaSpring.findById(Mockito.any())).thenReturn(this.optional);
        Exception thrown = assertThrows(UsuarioNaoEncontradoException.class, () -> {
            usuarioRepositorio.findById(MATRICULA);
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Usuário não encontrado na base de dados");
    }

    @Test
    @DisplayName("Search for an Usuario by EMAIL and return an existing successfully from DataBase")
    void testFindByEmailSuccess() throws Exception {
        this.optional = createOptionalUsuario();

        when(usuarioRepositorioJpaSpring.findByEmail(Mockito.any())).thenReturn(this.optional);
        UsuarioEntidade result = usuarioRepositorio.findByEmail(EMAIL);

        assertThat(result).isInstanceOf(UsuarioEntidade.class);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Search for an Usuario by EMAIL and return an excepiton because the EMAIL doesn't exist")
    void testFindByEmailUsuarioNaoEncontradoException() throws Exception {
        this.optional = Optional.empty();

        when(usuarioRepositorioJpaSpring.findByEmail(Mockito.any())).thenReturn(this.optional);
        Exception thrown = assertThrows(UsuarioNaoEncontradoException.class, () -> {
            usuarioRepositorio.findByEmail(EMAIL);
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Usuário não encontrado na base de dados");
    }

    @Test
    @DisplayName("Search for an Usuario by NOME and return an existing successfully from DataBase")
    void testFindByNomeSuccess() throws Exception {
        this.optional = createOptionalUsuario();

        when(usuarioRepositorioJpaSpring.findByNome(Mockito.any())).thenReturn(this.optional);
        UsuarioEntidade result = usuarioRepositorio.findByNome(NOME);

        assertThat(result).isInstanceOf(UsuarioEntidade.class);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Search for an Usuario by NOME and return an excepiton because the NOME doesn't exist")
    void testFindByNomeUsuarioNaoEncontradoException() throws Exception {
        this.optional = Optional.empty();

        when(usuarioRepositorioJpaSpring.findByNome(Mockito.any())).thenReturn(this.optional);
        Exception thrown = assertThrows(UsuarioNaoEncontradoException.class, () -> {
            usuarioRepositorio.findByNome(NOME);
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Usuário não encontrado na base de dados");
    }

    @Test
    @DisplayName("Search for  an Usuario by NOME and return an existing Optional successfully from DataBase")
    void testFindByNomeOptionalSuccess() throws Exception {
        this.optional = createOptionalUsuario();

        when(usuarioRepositorioJpaSpring.findByNome(Mockito.anyString())).thenReturn(this.optional);
        Optional<UsuarioEntidade> result = usuarioRepositorio.findByNomeOptional(NOME);

        assertThat(result).isNotNull();
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(UsuarioEntidade.class);
        assertThat(result.get()).isEqualTo(this.optional.get());
        assertThat(result.get().getNome()).isEqualTo(NOME);
    }

    @Test
    @DisplayName("Search for  an Usuario by EMAIL and return an existing Optional successfully from DataBase")
    void testFindByEmailOptionalSuccess() throws Exception {
        this.optional = createOptionalUsuario();

        when(usuarioRepositorioJpaSpring.findByEmail(Mockito.any())).thenReturn(this.optional);
        Optional<UsuarioEntidade> result = usuarioRepositorio.findByEmailOptional(EMAIL);

        assertThat(result).isNotNull();
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(UsuarioEntidade.class);
        assertThat(result.get()).isEqualTo(this.optional.get());
        assertThat(result.get().getEmail()).isEqualTo(EMAIL);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Should list all Usuarios successfully with Specification")
    void testListUsuariosSuccess() {
        this.entidade = createUsuarioEntidade();
        Page<UsuarioEntidade> page = new PageImpl<>(List.of(this.entidade));
        Pageable pageable = PageRequest.of(0, 10);
        Specification<UsuarioEntidade> specification = Specification.unrestricted();;

        when(usuarioRepositorioJpaSpring.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class))).thenReturn(page);
        Page<UsuarioEntidade> result = usuarioRepositorio.listUsuarios(specification, pageable);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Page.class);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getContent()).contains(this.entidade);
    }

    @Test
    @DisplayName("Should list all Usuarios by Curso successfully")
    void testListUsuariosByCursoSuccess() {
        this.entidade = createUsuarioEntidade();
        Page<UsuarioEntidade> page = new PageImpl<>(List.of(this.entidade));
        Pageable pageable = PageRequest.of(0, 10);

        when(usuarioRepositorioJpaSpring.findByCurso(Mockito.any(CursoEntidade.class), Mockito.any(Pageable.class))).thenReturn(page);
        Page<UsuarioEntidade> result = usuarioRepositorio.listUsuariosByCurso(pageable, CURSO_ENTIDADE);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Page.class);
        assertThat(result.getSize()).isNotNull();
        assertThat(result).hasSizeGreaterThan(0); 
    }

    @Test
    @DisplayName("Should create an Usuario successfully")
    void testCreateUsuarioSuccess() {
        this.entidade = createUsuarioEntidade();

        when(usuarioRepositorioJpaSpring.save(Mockito.any(UsuarioEntidade.class))).thenReturn(this.entidade);
        Integer result = usuarioRepositorio.createUsuario(entidade);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Integer.class);
        assertThat(result).isEqualTo(MATRICULA);
    }

    @Test
    @DisplayName("Should update an Usuario successfully")
    void testUpdateUsuarioSuccess() throws Exception {
        this.entidade = createUsuarioEntidade();
        this.optional = createOptionalUsuario();

        when(usuarioRepositorioJpaSpring.findById(Mockito.any())).thenReturn(this.optional);
        when(usuarioRepositorioJpaSpring.save(Mockito.any(UsuarioEntidade.class))).thenReturn(this.entidade);
        Integer result = usuarioRepositorio.updateUsuario(entidade);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Integer.class);
        assertThat(result).isEqualTo(MATRICULA);
    }

    @Test
    @DisplayName("Try to update an Usuario and return an excepiton because there isn't any Usuario with that MATRICULA")
    void testUpdateUsuarioUsuarioNaoEncontradoException() throws Exception {
        this.entidade = createUsuarioEntidade();
        this.optional = Optional.empty();

        when(usuarioRepositorioJpaSpring.findById(Mockito.any())).thenReturn(this.optional);
        Exception thrown = assertThrows(UsuarioNaoEncontradoException.class, () -> {
            usuarioRepositorio.updateUsuario(entidade);
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Usuário não encontrado na base de dados");
    }

    @Test
    @DisplayName("Should delete an Usuario successfully")
    void testDeleteSuccess() throws Exception{
        this.optional = createOptionalUsuario();

        when(usuarioRepositorioJpaSpring.findById(Mockito.any())).thenReturn(this.optional);
        doNothing().when(usuarioRepositorioJpaSpring).deleteById(Mockito.any());
        usuarioRepositorio.deleteUsuario(MATRICULA);

        verify(usuarioRepositorioJpaSpring, times(1)).deleteById(Mockito.any());
    }

    @Test
    @DisplayName("Try to delete an Usuario and return an excepiton because there isn't any Usuario with that MATRICULA")
    void testDeleteUsuarioUsuarioNaoEncontradoException() throws Exception{
        this.optional = Optional.empty();

        when(usuarioRepositorioJpaSpring.findById(Mockito.any())).thenReturn(this.optional);
        Exception thrown = assertThrows(UsuarioNaoEncontradoException.class, () -> {
            usuarioRepositorio.deleteUsuario(MATRICULA);
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Usuário não encontrado na base de dados");
    }

    @Test
    @DisplayName("Should delete all Usuarios successfully")
    void testDeleteAllSuccess() {
        doNothing().when(usuarioRepositorioJpaSpring).deleteAll();
        usuarioRepositorio.deleteAll();

        verify(usuarioRepositorioJpaSpring, times(1)).deleteAll();
    }
    
    private Optional<UsuarioEntidade> createOptionalUsuario(){
        UsuarioEntidade usuario = new UsuarioEntidade(MATRICULA, CURSO_ENTIDADE, NOME, EMAIL, SENHA, TIPO, DATA);
    
        Optional<UsuarioEntidade> usuárioOptional = Optional.ofNullable(usuario);
        return usuárioOptional;
    }

    private UsuarioEntidade createUsuarioEntidade(){
        UsuarioEntidade usuario = new UsuarioEntidade(MATRICULA, CURSO_ENTIDADE, NOME, EMAIL, SENHA, TIPO, DATA);
        return usuario;
    }
}
