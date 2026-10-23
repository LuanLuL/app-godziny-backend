package com.cefet.godziny.domain.casouso.atividade.arquivo;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.cefet.godziny.infraestrutura.exceptions.atividade.arquivo.ArquivoInvalidoException;
import com.cefet.godziny.infraestrutura.persistencia.atividade.arquivo.ArquivoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.atividade.arquivo.ArquivoRepositorioJpaSpring;
import com.cefet.godziny.infraestrutura.rest.atividade.arquivo.ArquivoRestConverter;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@AllArgsConstructor
@Setter
public class CriarArquivoCasoUso {

    private final ArquivoRepositorioJpaSpring arquivoRepositorioJpa; 

    @NotNull(message = "O nome do arquivo é obrigatório")
    private String nome;

    public void validarCriacao() throws Exception {
        this.nome = StringUtils.cleanPath(this.nome);
        if(this.nome.contains((".."))){
            throw new ArquivoInvalidoException("O nome do arquivo fornecido como comprovante contém um caminho inválido: "
            + this.nome);
        }
    }

    public ArquivoEntidade createArquivo(MultipartFile arquivo) throws Exception{
        return arquivoRepositorioJpa.save(ArquivoRestConverter.MultipartFileToEntidadeJpa(arquivo, this.nome));
    }
}
