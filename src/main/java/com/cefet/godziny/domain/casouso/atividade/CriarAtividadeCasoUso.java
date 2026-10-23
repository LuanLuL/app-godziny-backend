package com.cefet.godziny.domain.casouso.atividade;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import com.cefet.godziny.api.atividade.AtividadeDto;
import com.cefet.godziny.infraestrutura.exceptions.atividade.CriarAtividadeIncompletaException;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.atividade.arquivo.ArquivoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.atividade.arquivo.ArquivoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpa;
import com.cefet.godziny.infraestrutura.rest.atividade.AtividadeRestConverter;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@AllArgsConstructor
@Setter
public class CriarAtividadeCasoUso {

    private final AtividadeRepositorioJpa atividadeRepositorioJpa;

    private final CategoriaRepositorioJpa categoriaRepositorioJpa;

    private final UsuarioRepositorioJpa usuarioRepositorioJpa;

    private final ArquivoRepositorioJpa arquivoRepositorioJpa;

    @NotNull(message = "O título da atividade é obrigatório")
    private String titulo;

    @NotNull(message = "A data de criaçao da atividade é obrigatória")
    private LocalDateTime createdAt;

    public void validarCriacao() throws Exception {
        if (this.titulo.length() < 3 || this.titulo.length() > 500) {
            throw new CriarAtividadeIncompletaException("O título da atividade deve ter entre 3 e 500 caracteres");
        }
        LocalDateTime now = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        if (this.createdAt.isAfter(now)) {
            throw new CriarAtividadeIncompletaException("A data de criação da atividade deve ser menor ou igual à data e hora atuais");
        }
    }

    public UUID createAtividade(AtividadeDto dto, MultipartFile arquivo) throws Exception{
        ArquivoEntidade arquivoEntidade = arquivoRepositorioJpa.createArquivo(arquivo, arquivo.getOriginalFilename());
        dto.setArquivoId(arquivoEntidade.getId());
        return atividadeRepositorioJpa.createAtividade(AtividadeRestConverter.DtoToEntidadeJpa(
            dto,
            usuarioRepositorioJpa.findById(dto.getUsuarioId()),
            categoriaRepositorioJpa.findById(dto.getCategoriaId()),
            arquivoEntidade
        ));
    }
}
