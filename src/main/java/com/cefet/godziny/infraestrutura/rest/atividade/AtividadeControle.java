package com.cefet.godziny.infraestrutura.rest.atividade;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.cefet.godziny.api.atividade.AtividadeAtualizarDto;
import com.cefet.godziny.api.atividade.AtividadeFiltroDto;
import com.cefet.godziny.api.atividade.AtividadeDto;
import com.cefet.godziny.api.atividade.AtividadeRecuperarDto;
import com.cefet.godziny.api.atividade.IAtividadeApi;
import com.cefet.godziny.domain.casouso.atividade.AtualizarAtivdadeCasoUso;
import com.cefet.godziny.domain.casouso.atividade.CriarAtividadeCasoUso;
import com.cefet.godziny.domain.casouso.atividade.ListarAtividadeCasoUso;
import com.cefet.godziny.domain.casouso.atividade.PesquisarAtividadeCasoUso;
import com.cefet.godziny.domain.casouso.atividade.RemoverAtividadeCasoUso;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.atividade.arquivo.ArquivoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpa;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AtividadeControle implements IAtividadeApi {

    private final AtividadeRepositorioJpa atividadeRepositorioJpa;

    private final UsuarioRepositorioJpa usuarioRepositorioJpa;

    private final CursoRepositorioJpa cursoRepositorioJpa;

    private final CategoriaRepositorioJpa categoriaRepositorioJpa;

    private final ArquivoRepositorioJpa arquivoRepositorioJpa;

    @Override
    public ResponseEntity<AtividadeRecuperarDto> getAtividade(UUID id) throws Exception {
        ListarAtividadeCasoUso casoUso = new ListarAtividadeCasoUso(atividadeRepositorioJpa, id);
        return ResponseEntity.status(HttpStatus.OK).body(casoUso.validarListagem());
    }

    @Override
    public ResponseEntity<Page<AtividadeRecuperarDto>> pesquisarAtividades(Pageable pageable, AtividadeFiltroDto atividadeFiltroDto) throws Exception {
        PesquisarAtividadeCasoUso casoUso = new PesquisarAtividadeCasoUso(atividadeRepositorioJpa, cursoRepositorioJpa, atividadeFiltroDto.getUsuarioNome(), atividadeFiltroDto.getTitulo(), atividadeFiltroDto.getStatus(), atividadeFiltroDto.getCategoria());
        casoUso.validarPesquisa();
        return ResponseEntity.status(HttpStatus.OK).body(casoUso.pesquisarAtividade(pageable));
    }

    @Override
    public ResponseEntity<UUID> createAtividade(@Valid @RequestPart("dto") AtividadeDto dto, @RequestPart("arquivo") MultipartFile arquivo) throws Exception {
        CriarAtividadeCasoUso casoUso = AtividadeRestConverter.DtoToCriarAtividadeCasoUso(dto, atividadeRepositorioJpa, categoriaRepositorioJpa, usuarioRepositorioJpa, arquivoRepositorioJpa);
        casoUso.validarCriacao();
        return ResponseEntity.status(HttpStatus.CREATED).body(casoUso.createAtividade(dto, arquivo));
    }

    @Override
    public ResponseEntity<UUID> updateAtividade(UUID atividadeId, @Valid AtividadeAtualizarDto dto) throws Exception {
        AtualizarAtivdadeCasoUso casoUso = AtividadeRestConverter.DtoToUpdateCursoCasoUso(dto, atividadeRepositorioJpa, categoriaRepositorioJpa, usuarioRepositorioJpa, arquivoRepositorioJpa);
        casoUso.validarAtualizacao();
        return ResponseEntity.status(HttpStatus.OK).body(casoUso.atualizarAtividade(dto));
    }
   

    @Override
    public ResponseEntity<Void> removeAtividade(UUID atividadeId) throws Exception {
        RemoverAtividadeCasoUso casoUso = new RemoverAtividadeCasoUso(atividadeId, atividadeRepositorioJpa, arquivoRepositorioJpa);
        casoUso.removerAtividade(casoUso.validarRemocao());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}

