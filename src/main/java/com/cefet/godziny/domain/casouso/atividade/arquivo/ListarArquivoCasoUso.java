package com.cefet.godziny.domain.casouso.atividade.arquivo;

import java.util.UUID;
import com.cefet.godziny.infraestrutura.persistencia.atividade.arquivo.ArquivoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.atividade.arquivo.ArquivoRepositorioJpa;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ListarArquivoCasoUso {

    private final ArquivoRepositorioJpa arquivoRepositorioJpa;

    @NotNull(message = "O ID do arquivo é obrigatório")
    private UUID id;

    public ArquivoEntidade validarListagem() throws Exception {
        return arquivoRepositorioJpa.findById(this.id);
    }
}
