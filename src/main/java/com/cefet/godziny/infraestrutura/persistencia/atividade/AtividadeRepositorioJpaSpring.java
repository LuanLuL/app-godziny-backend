package com.cefet.godziny.infraestrutura.persistencia.atividade;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;

public interface AtividadeRepositorioJpaSpring extends JpaRepository<AtividadeEntidade, UUID>, JpaSpecificationExecutor<AtividadeEntidade> {
    
    List<AtividadeEntidade> findByCategoria(CategoriaEntidade categoria);

    List<AtividadeEntidade> findByUsuario(UsuarioEntidade usuario);
    
    @Query("SELECT SUM(a.cargaHoraria) FROM atividade a WHERE a.usuario.matricula = :usuarioId AND a.categoria.id = :categoriaId AND a.id <> :atividadeId")
    Float sumCargaHorariaByUsuarioAndCategoria(
        @Param("usuarioId") Integer usuarioId,
        @Param("categoriaId") UUID categoriaId,
        @Param("atividadeId") UUID atividadeId
    );
}
