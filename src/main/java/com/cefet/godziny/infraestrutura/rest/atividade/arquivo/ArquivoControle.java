package com.cefet.godziny.infraestrutura.rest.atividade.arquivo;

import java.util.UUID;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.cefet.godziny.api.atividade.arquivo.IArquivoApi;
import com.cefet.godziny.domain.casouso.atividade.arquivo.ListarArquivoCasoUso;
import com.cefet.godziny.infraestrutura.persistencia.atividade.arquivo.ArquivoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.atividade.arquivo.ArquivoRepositorioJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.Resource;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class ArquivoControle implements IArquivoApi {

    private final ArquivoRepositorioJpa arquivoRepositorioJpa;

    @Override
    public ResponseEntity<Resource> getArquivo(UUID id) throws Exception {
        ListarArquivoCasoUso casoUso = new ListarArquivoCasoUso(arquivoRepositorioJpa, id);
        ArquivoEntidade arquivo = casoUso.validarListagem();
        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.parseMediaType(arquivo.getTipo()))
            .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "arquivo; filename=\"" + arquivo.getNome() + "\"")
            .body(new ByteArrayResource(arquivo.getDado()));
    }
}
