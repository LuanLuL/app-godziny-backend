package com.cefet.godziny.infraestrutura.persistencia.usuario;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UsuarioRepositorioJpaSpring extends JpaRepository<UsuarioEntidade, Integer>, JpaSpecificationExecutor<UsuarioEntidade> {
    Page<UsuarioEntidade> findByCurso(CursoEntidade curso, Pageable pageable);

    Optional<UsuarioEntidade> findByEmail(String email);

    Optional<UsuarioEntidade> findByNome(String nome);
}