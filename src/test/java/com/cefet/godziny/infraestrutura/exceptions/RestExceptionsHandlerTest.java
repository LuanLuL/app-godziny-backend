package com.cefet.godziny.infraestrutura.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.cefet.godziny.infraestrutura.exceptions.atividade.AtividadeNaoEncontradaException;
import com.cefet.godziny.infraestrutura.exceptions.atividade.AtualizarAtividadeStatusErradoException;
import com.cefet.godziny.infraestrutura.exceptions.atividade.CriarAtividadeIncompletaException;
import com.cefet.godziny.infraestrutura.exceptions.atividade.LimiteCargaHorariaExcedidoException;
import com.cefet.godziny.infraestrutura.exceptions.atividade.RemoverAtividadeSimulandoOuAprovadaException;
import com.cefet.godziny.infraestrutura.exceptions.atividade.arquivo.ArquivoInvalidoException;
import com.cefet.godziny.infraestrutura.exceptions.atividade.arquivo.ArquivoNaoEncontradoException;
import com.cefet.godziny.infraestrutura.exceptions.auth.AuthEmailOuSenhaIncorretoException;
import com.cefet.godziny.infraestrutura.exceptions.categoria.CategoriaNaoEncontradaException;
import com.cefet.godziny.infraestrutura.exceptions.categoria.CriarCategoriaIncompletaException;
import com.cefet.godziny.infraestrutura.exceptions.categoria.RemoverCategoriaComAtividadesException;
import com.cefet.godziny.infraestrutura.exceptions.curso.CriarCursoComUsuarioNormalException;
import com.cefet.godziny.infraestrutura.exceptions.curso.CriarCursoIncompletoException;
import com.cefet.godziny.infraestrutura.exceptions.curso.CursoNaoEncontradoException;
import com.cefet.godziny.infraestrutura.exceptions.curso.RemoverCursoComCategoriasException;
import com.cefet.godziny.infraestrutura.exceptions.curso.RemoverCursoComUsuariosException;
import com.cefet.godziny.infraestrutura.exceptions.usuario.CriarUsuarioAdmComCursoException;
import com.cefet.godziny.infraestrutura.exceptions.usuario.CriarUsuarioEmailRepetidoException;
import com.cefet.godziny.infraestrutura.exceptions.usuario.CriarUsuarioIncompletoException;
import com.cefet.godziny.infraestrutura.exceptions.usuario.CriarUsuarioNormalSemCursoException;
import com.cefet.godziny.infraestrutura.exceptions.usuario.RemoverUsuarioComAtividadesException;
import com.cefet.godziny.infraestrutura.exceptions.usuario.UsuarioNaoEncontradoException;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RestExceptionsHandlerTest {

    private RestExceptionsHandler restExceptionsHandler;

    @BeforeEach
    public void setup() {
        restExceptionsHandler = new RestExceptionsHandler();
    }

    @Test
    @DisplayName("Should create a CriarCursoIncompletoException successfully")
    public void testCriarCursoIncompletoException() throws Exception {
        CriarCursoIncompletoException exception = new CriarCursoIncompletoException("Curso incompleto");

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.criarCursoInclompletoException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testCursoNaoEncontradoException() throws Exception {
        CursoNaoEncontradoException exception = new CursoNaoEncontradoException("Curso não encontrado");

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.cursoNaoEncontradoException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testRemoverCursoComUsuariosException() throws Exception {
        RemoverCursoComUsuariosException exception = new RemoverCursoComUsuariosException();

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.removerCursoComUsuarioException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    public void testCriarUsuarioIncompletoException() throws Exception {
        CriarUsuarioIncompletoException exception = new CriarUsuarioIncompletoException("Usuário incompleto");

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.criarUsuarioIncompletoException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testCriarUsuarioNormalSemCurso() throws Exception {
        CriarUsuarioNormalSemCursoException exception = new CriarUsuarioNormalSemCursoException();

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.criarUsuarioNormalSemCursoException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testCriarUsuarioAdmComCurso() throws Exception {
        CriarUsuarioAdmComCursoException exception = new CriarUsuarioAdmComCursoException();

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.criarUsuarioAdmComCursoException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testCriarUsuarioEmailRepetidoException() throws Exception {
        CriarUsuarioEmailRepetidoException exception = new CriarUsuarioEmailRepetidoException();

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.criarUsuarioEmailRepetidoException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    public void testUsuarioNaoEncontradoException() throws Exception {
        UsuarioNaoEncontradoException exception = new UsuarioNaoEncontradoException();

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.usuarioNaoEncontradoException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testRemoverUsuarioComAtividadesException() throws Exception {
        RemoverUsuarioComAtividadesException exception = new RemoverUsuarioComAtividadesException();

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.removerUsuarioComAtividadesException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    public void testCategoriaNaoEncontradoException() throws Exception {
        CategoriaNaoEncontradaException exception = new CategoriaNaoEncontradaException();

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.categoriaNaoEncontradaException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testCriarCategoriaIncompletaException() throws Exception {
        CriarCategoriaIncompletaException exception = new CriarCategoriaIncompletaException("Categora incompleta");

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.criarCategoriaInconpletaException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testRemoverCursoComCategoriasException() throws Exception {
        RemoverCursoComCategoriasException exception = new RemoverCursoComCategoriasException();

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.removerCursoComCategoriaException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    public void testCampoRepetidoNoBancoException() throws Exception {
        CampoRepetidoNoBancoException exception = new CampoRepetidoNoBancoException("Campo repetido no banco de dados");

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.campoRepetidoNoBancoException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    public void testRemoverCategoriaComAtividadesException() throws Exception {
        RemoverCategoriaComAtividadesException exception = new RemoverCategoriaComAtividadesException();

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.removerCategoriaComAtividadesException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    public void testAtividadeNaoEncontradaException() throws Exception {
        AtividadeNaoEncontradaException exception = new AtividadeNaoEncontradaException();

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.atividadeNaoEncontradaException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    
    @Test
    public void testCriarAtividadeIncompletaException() throws Exception {
        CriarAtividadeIncompletaException exception = new CriarAtividadeIncompletaException("Atividade incompleta");

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.criarAtividadeIncompletaException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testLimiteCargaHorariaExcedidoException() throws Exception {
        LimiteCargaHorariaExcedidoException exception = new LimiteCargaHorariaExcedidoException("Carga horaria ultrapassou o limite");

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.limiteCargaHorariaExcedidoException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testAtualizarAtividadeStatusErradoException() throws Exception {
        AtualizarAtividadeStatusErradoException exception = new AtualizarAtividadeStatusErradoException("Não é possível atualizar atividades em simulação");

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.atualizarAtividadeStatusErradoException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testRemoverAtividadeSimulandoOuAprovadaException() throws Exception {
        RemoverAtividadeSimulandoOuAprovadaException exception = new RemoverAtividadeSimulandoOuAprovadaException();

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.removerAtividadeSimulandoOuAprovadaException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testArquivoNaoEncontradoException() throws Exception {
        ArquivoNaoEncontradoException exception = new ArquivoNaoEncontradoException();

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.arquivoNaoEncontradoException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testArquivoInvalidoException() throws Exception {
        ArquivoInvalidoException exception = new ArquivoInvalidoException("Nome do arquivo inválido");

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.arquivoInvalidoException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testAuthEmailOuSenhaIncorretoException() throws Exception {
        AuthEmailOuSenhaIncorretoException exception = new AuthEmailOuSenhaIncorretoException();

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.authEmailOuSenhaIncorretoException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void testCriarCursoComUsuarioNormalException() throws Exception {
        CriarCursoComUsuarioNormalException exception = new CriarCursoComUsuarioNormalException();

        ResponseEntity<RestDefaultErrorMessage> response = restExceptionsHandler.criarCursoComUsuarioNormalException(exception);

        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testUsuarioNaoAutorizadoException() {
        UsuarioNaoAutorizadoException exception =
            new UsuarioNaoAutorizadoException();

        ResponseEntity<RestDefaultErrorMessage> response =
            restExceptionsHandler.usuarioNaoAutorizadoException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isInstanceOf(RestDefaultErrorMessage.class);
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getDetail()).isEqualTo(exception.getMessage());
    }
}