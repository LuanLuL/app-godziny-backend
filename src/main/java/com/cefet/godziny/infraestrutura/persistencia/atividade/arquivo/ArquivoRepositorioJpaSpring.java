package com.cefet.godziny.infraestrutura.persistencia.atividade.arquivo;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArquivoRepositorioJpaSpring extends JpaRepository<ArquivoEntidade, UUID>{}
