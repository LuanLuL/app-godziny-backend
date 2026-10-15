package com.cefet.godziny.infraestrutura.persistencia.curso;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.cefet.godziny.domain.porta.curso.ICursoRepositorio;
import com.cefet.godziny.infraestrutura.rest.curso.CursoRestConverter;

@Component
public class CursoRepositorioJpa implements ICursoRepositorio {

    private final CursoRepositorioJpaSpring repositorio;

    public CursoRepositorioJpa(CursoRepositorioJpaSpring repositorio){
        this.repositorio = repositorio;
    }

    @Override
    public CursoEntidade findBySigla(String sigla) throws Exception {
        if(sigla == ""){
            return null;
        }
        Optional<CursoEntidade> curso = this.repositorio.findBySigla(sigla);
        return CursoRestConverter.OptionalToCursoEntidade(curso);
    }

    @Override
    public Optional<CursoEntidade> findBySiglaOptional(String sigla) {
        if(sigla == ""){
            return null;
        }
        return this.repositorio.findBySigla(sigla);
    }

    @Override
    @Transactional
    public List<UUID> findByCoordenador(Integer matricula) {
       return this.repositorio.findByAdmin(matricula);
    }

    @Override
    public Page<CursoEntidade> listCursos(Specification<CursoEntidade> specification, Pageable pageable) {
        return repositorio.findAll(specification, pageable);
    }

    @Override
    public String createCurso(CursoEntidade newCurso) {
        return repositorio.save(newCurso).getSigla();
    }

    @Override
    @Transactional
    public String updateCurso(String cursoSigla, CursoEntidade newCurso) throws Exception {
        Optional<CursoEntidade> curso = this.repositorio.findBySigla(cursoSigla);
        CursoRestConverter.OptionalToCursoEntidade(curso);
        repositorio.updateCursoById(cursoSigla, newCurso.getSigla(), newCurso.getCarga_horaria_complementar(), newCurso.getNome());
        return newCurso.getSigla();
    }

    @Override
    public void deleteCurso(String sigla) throws Exception {
        Optional<CursoEntidade> curso = this.repositorio.findBySigla(sigla);
        CursoRestConverter.OptionalToCursoEntidade(curso);
        repositorio.deleteBySigla(sigla);
    }

    @Override
    public void deleteAll(){
        repositorio.deleteAll();
    }
}
