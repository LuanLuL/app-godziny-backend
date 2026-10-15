package com.cefet.godziny.infraestrutura.persistencia.categoria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;

public interface CategoriaRepositorioJpaSpring extends JpaRepository<CategoriaEntidade, UUID>, JpaSpecificationExecutor<CategoriaEntidade> {
    
    List<CategoriaEntidade> findByCursoAndNome(CursoEntidade curso, String nome);

    List<CategoriaEntidade> findByCurso(CursoEntidade curso);

    Optional<CategoriaEntidade> findByNome(String nome);
    
}
