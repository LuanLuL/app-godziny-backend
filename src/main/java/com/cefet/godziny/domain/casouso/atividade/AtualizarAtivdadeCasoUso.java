package com.cefet.godziny.domain.casouso.atividade;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.UUID;
import com.cefet.godziny.api.atividade.AtividadeAtualizarDto;
import com.cefet.godziny.constantes.atividade.EnumStatus;
import com.cefet.godziny.infraestrutura.exceptions.atividade.AtualizarAtividadeStatusErradoException;
import com.cefet.godziny.infraestrutura.exceptions.atividade.CriarAtividadeIncompletaException;
import com.cefet.godziny.infraestrutura.exceptions.atividade.LimiteCargaHorariaExcedidoException;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.atividade.arquivo.ArquivoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.atividade.arquivo.ArquivoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaEntidade;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpa;
import com.cefet.godziny.infraestrutura.rest.atividade.AtividadeRestConverter;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@AllArgsConstructor
@Setter
public class AtualizarAtivdadeCasoUso {

    private final AtividadeRepositorioJpa atividadeRepositorioJpa;

    private final CategoriaRepositorioJpa categoriaRepositorioJpa;

    private final UsuarioRepositorioJpa usuarioRepositorioJpa;

    private final ArquivoRepositorioJpa arquivoRepositorioJpa;

    @NotNull(message = "O ID da atividade é obrigatório")
    private UUID atividadeId;

    @NotNull(message = "A matrícula do usuário da atividade é obrigatório")
    private Integer usuarioId;

    @NotNull(message = "O ID da categoria da atividade é obrigatório")
    private UUID categoriaId;
   
    @NotNull(message = "A carga horária da atividade é obrigatória")
    private float cargaHoraria;

    @NotNull(message = "O comentário da atividade é obrigatório")
    private String comentario;

    @NotNull(message = "O status da atividade é obrigatório")
    private EnumStatus status;

    public void validarAtualizacao() throws Exception {
        if (this.cargaHoraria < 0) {
            throw new CriarAtividadeIncompletaException("A carga horária da atividade deve ser maior ou igual que zero");
        }
        if (this.comentario.length() < 2 || this.comentario.length() > 500) {
            throw new CriarAtividadeIncompletaException("O comentário da atividade deve ter entre 2 e 500 caracteres");
        }
        if(this.status.equals(EnumStatus.SIMULANDO)){
            throw new AtualizarAtividadeStatusErradoException("Não é possível atualizar atividades em simulação");
        } else if(this.status.equals(EnumStatus.APROVADA) && this.cargaHoraria == 0){
            throw new AtualizarAtividadeStatusErradoException("Não é possível aprovar uma atividade com carga de zero horas");
        } else if(this.status.equals(EnumStatus.REJEITADA) && this.cargaHoraria > 0){
            throw new AtualizarAtividadeStatusErradoException("Não é possível aprovar horas em uma atividade rejeitada");
        }
        Float cargaHorariaTotalUsuarioNaCategoria = this.atividadeRepositorioJpa
                .sumCargaHorarioByUsuarioIdAndCategoriaId(this.usuarioId, this.categoriaId, this.atividadeId);
        if(cargaHorariaTotalUsuarioNaCategoria == null){
            cargaHorariaTotalUsuarioNaCategoria = (float) 0.0;
        }
        CategoriaEntidade categoriaEntidade = this.categoriaRepositorioJpa.findById(this.categoriaId);
        float cargaHorariaMaximaCategoria = categoriaEntidade.getCurso().getCarga_horaria_complementar() 
                                            * categoriaEntidade.getPorcentagemHorasMaximas();
        float novaCargaHoraria = this.cargaHoraria * categoriaEntidade.getHorasMultiplicador();
        if (cargaHorariaTotalUsuarioNaCategoria + novaCargaHoraria > cargaHorariaMaximaCategoria) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
            symbols.setDecimalSeparator(',');
            DecimalFormat df = new DecimalFormat("#.##", symbols);
            throw new LimiteCargaHorariaExcedidoException("A carga horária total deste usuário para esta categoria irá exceder o limite permitido em " + 
            df.format(((cargaHorariaTotalUsuarioNaCategoria + novaCargaHoraria) - cargaHorariaMaximaCategoria)) + " horas.");
        }
    }

    public UUID atualizarAtividade(AtividadeAtualizarDto dto) throws Exception{
        ArquivoEntidade arquivoEntidade = arquivoRepositorioJpa.findById(dto.getArquivoId());
        return atividadeRepositorioJpa.updateAtividade(AtividadeRestConverter.DtoToEntidadeJpa(
            dto,
            usuarioRepositorioJpa.findById(dto.getUsuarioId()),
            categoriaRepositorioJpa.findById(dto.getCategoriaId()),
            arquivoEntidade
        ));
    }
}