package com.cefet.godziny.domain.casouso.atividade;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import com.cefet.godziny.constantes.atividade.EnumStatus;
import com.cefet.godziny.infraestrutura.exceptions.atividade.RemoverAtividadeSimulandoOuAprovadaException;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeEntidade;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.atividade.arquivo.ArquivoRepositorioJpa;

import lombok.*;

@AllArgsConstructor
public class RemoverAtividadeCasoUso {
    @NotNull(message = "O ID da atividade é obrigatório")
    private UUID atividadeId;

    private final AtividadeRepositorioJpa atividadeRepositorioJpa;

    private final ArquivoRepositorioJpa arquivoRepositorioJpa;


    public AtividadeEntidade validarRemocao() throws Exception {
        AtividadeEntidade atividadeEntidade = atividadeRepositorioJpa.findById(atividadeId);
        if(atividadeEntidade.getStatus() != EnumStatus.REJEITADA){
            throw new RemoverAtividadeSimulandoOuAprovadaException();
        }
        return atividadeEntidade;
    }

    public void removerAtividade(AtividadeEntidade entidade) throws Exception {
        atividadeRepositorioJpa.deleteAtividade(this.atividadeId);
        arquivoRepositorioJpa.deleteArquivo(entidade.getArquivo().getId());
    }
}
