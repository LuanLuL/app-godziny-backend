package com.cefet.godziny.domain.casouso.atividade;

import java.util.UUID;
import com.cefet.godziny.api.atividade.AtividadeRecuperarDto;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeEntidade;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeRepositorioJpa;
import com.cefet.godziny.infraestrutura.rest.atividade.AtividadeRestConverter;
import com.cefet.godziny.infraestrutura.rest.categoria.CategoriaRestConverter;
import com.cefet.godziny.infraestrutura.rest.usuario.UsuarioRestConverter;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ListarAtividadeCasoUso {

    private final AtividadeRepositorioJpa atividadeRepositorioJpa;

    @NotNull(message = "O ID da atividade é obrigatório")
    private UUID id;

    public AtividadeRecuperarDto validarListagem() throws Exception {
        AtividadeEntidade entidade = atividadeRepositorioJpa.findById(this.id);
        createAtividadeRecuperarDto(entidade);
        return AtividadeRestConverter.EntidadeToAtividadeRecuperarDto(entidade);
    }

    /*
    public Page<AtividadeRecuperarDto> listarAtividades(Pageable pageable) {
        Page<AtividadeRecuperarDto> pageAtividadeDto = atividadeRepositorioJpa.listAtividades(pageable).map(
            atividadeEntidade -> AtividadeRestConverter.EntidadeToAtividadeRecuperarDto(
                atividadeEntidade
        ));
        return pageAtividadeDto;
    }
*/
    private AtividadeRecuperarDto createAtividadeRecuperarDto(AtividadeEntidade atividadeEntidade){
        AtividadeRecuperarDto dto = new  AtividadeRecuperarDto();
        dto.setId(atividadeEntidade.getId());
        dto.setUsuario(UsuarioRestConverter.EntidadeToUsuarioRecuperarDto(atividadeEntidade.getUsuario()));
        dto.setCategoria(CategoriaRestConverter.EntidadeToCategoriaRecuperarDto(atividadeEntidade.getCategoria()));
        dto.setTitulo(atividadeEntidade.getTitulo());
        dto.setCreatedAt(atividadeEntidade.getCreatedAt());
        dto.setStatus(atividadeEntidade.getStatus());
        dto.setArquivoId(atividadeEntidade.getArquivo().getId());
        dto.setCargaHoraria(atividadeEntidade.getCargaHoraria());
        dto.setComentario(atividadeEntidade.getComentario());
        dto.getId();
        dto.getUsuario();
        dto.getCategoria();
        dto.getTitulo();
        dto.getCreatedAt();
        dto.getStatus();
        dto.getArquivoId();
        dto.getCargaHoraria();
        dto.getComentario();
        return dto;
    }
}
