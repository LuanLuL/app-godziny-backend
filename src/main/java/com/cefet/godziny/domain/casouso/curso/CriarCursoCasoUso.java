package com.cefet.godziny.domain.casouso.curso;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.cefet.godziny.api.curso.CursoDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.CampoRepetidoNoBancoException;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.exceptions.curso.CriarCursoComUsuarioNormalException;
import com.cefet.godziny.infraestrutura.exceptions.curso.CriarCursoIncompletoException;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpa;
import com.cefet.godziny.infraestrutura.rest.curso.CursoRestConverter;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@AllArgsConstructor
@Setter
public class CriarCursoCasoUso {

    private final CursoRepositorioJpa cursoRepositorioJpa;

    private final UsuarioRepositorioJpa usuarioRepositorioJpa;

    @NotNull(message = "A sigla do curso é obrigatória")
    private String sigla;

    @NotNull(message = "O nome do curso é obrigatório")
    private String nome;

    @NotNull(message = "A carga de horas complementares do curso é obrigatória")
    private int cargaHorariaComplementar;

    @NotNull(message = "A matricula do coordenador do curso é obrigatório")
    private Integer coordenador_matricula;

    public UsuarioEntidade validarCriacao() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsuarioEntidade userContext = (UsuarioEntidade) authentication.getPrincipal();
        if(!userContext.getTipo().equals(EnumRecursos.ADM)){
            throw new UsuarioNaoAutorizadoException();
        }
        if (sigla.length() < 3 || sigla.length() > 20) {
            throw new CriarCursoIncompletoException("A sigla do curso deve ter entre 3 e 20 caracteres");
        }
        if (nome.length() < 3 || nome.length() > 50) {
            throw new CriarCursoIncompletoException("O nome do curso deve ter entre 3 e 50 caracteres");
        }
        if (cargaHorariaComplementar < 100 || cargaHorariaComplementar > 800) {
            throw new CriarCursoIncompletoException("A carga de horas complementares do curso deve estar entre 100 e 800");
        }
        Optional<CursoEntidade> entidade = cursoRepositorioJpa.findBySiglaOptional(this.sigla);
        if(entidade.isPresent()){
           throw new CampoRepetidoNoBancoException("Já existe um Curso com essa sigla cadastrado na base de dados"); 
        }
        UsuarioEntidade coordenador = usuarioRepositorioJpa.findById(coordenador_matricula);
        if(coordenador.getTipo().equals(EnumRecursos.NORMAL)){
            throw new CriarCursoComUsuarioNormalException();
        }
        return coordenador;
    }

    public String createCurso(CursoDto dto, UsuarioEntidade coordenador) throws Exception{
        var cursoEntidade = CursoRestConverter.DtoToEntidadeJpa(dto, coordenador);
        String sigla = cursoRepositorioJpa.createCurso(cursoEntidade);
        return sigla;
    }
}

