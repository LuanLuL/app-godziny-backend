package com.cefet.godziny.domain.casouso.categoria;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.cefet.godziny.api.categoria.CategoriaDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.CampoRepetidoNoBancoException;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.exceptions.categoria.CriarCategoriaIncompletaException;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaEntidade;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.rest.categoria.CategoriaRestConverter;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@AllArgsConstructor
@Setter
public class AtualizarCategoriaCasoUso {

    private final CategoriaRepositorioJpa categoriaRepositorioJpa;

    private final CursoRepositorioJpa cursoRepositorioJpa;

    @NotNull(message = "O id da categoria é obrigatório")
    private UUID categoriaId;

    @NotNull(message = "A sigla do curso é obrigatória")
    private String cursoSigla;

    @NotNull(message = "O nome da categoria é obrigatório")
    private String nome;

    @NotNull(message = "O número de horas máximas da categoria é obrigatório")
    private float porcentagemHorasMaximas;

    @NotNull(message = "O multiplicador por horas da categoria é obrigatório")
    private float horasMultiplicador;

    @NotNull(message = "A descrição da categoria é obrigatória")
    private String descricao;
        

    public CursoEntidade validarAtualizacao() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsuarioEntidade userContext = (UsuarioEntidade) authentication.getPrincipal();
        if(!userContext.getTipo().equals(EnumRecursos.ADM)){
            throw new UsuarioNaoAutorizadoException();
        }
        if (this.cursoSigla.length() < 3 || this.cursoSigla.length() > 20) {
            throw new CriarCategoriaIncompletaException("A sigla do curso na categoria deve ter entre 3 e 20 caracteres");
        }
        if (this.nome.length() < 3 || this.nome.length() > 250) {
            throw new CriarCategoriaIncompletaException("O nome da categoria deve ter entre 3 e 250 caracteres");
        }
        if (this.porcentagemHorasMaximas <= 0) {
            throw new CriarCategoriaIncompletaException("A porcentagem de horas máximas da categoria deve ser maior que zero");
        }
        if (this.horasMultiplicador <= 0) {
            throw new CriarCategoriaIncompletaException("O multiplicador por horas da categoria deve ser maior que zero");
        }
        if (this.descricao.length() < 10 || this.descricao.length() > 500) {
            throw new CriarCategoriaIncompletaException("A descrição da categoria deve ter entre 10 e 500 caracteres");
        }
        CursoEntidade cursoEntidade = cursoRepositorioJpa.findBySigla(this.cursoSigla);
        List<CategoriaEntidade> listCategoria = categoriaRepositorioJpa.findByCursoAndNome(cursoEntidade, this.nome);
        if(!listCategoria.isEmpty() && !listCategoria.stream().anyMatch(element -> element.getId().equals(this.categoriaId))){
            throw new CampoRepetidoNoBancoException("Já existe uma categoria com este nome no curso selecionado");
        }
        return cursoEntidade;
    }

    public UUID atualizarCategoria(CategoriaDto dto, CursoEntidade cursoEntidade) throws Exception{
        return categoriaRepositorioJpa.updateCategoria(CategoriaRestConverter.DtoToEntidadeJpa(dto, cursoEntidade));
    }
}
