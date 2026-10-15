package com.cefet.godziny.infraestrutura.rest.usuario;

import org.springframework.web.bind.annotation.RestController;
import com.cefet.godziny.api.usuario.IUsuarioApi;
import com.cefet.godziny.api.usuario.UsuarioDto;
import com.cefet.godziny.api.usuario.UsuarioFiltroDto;
import com.cefet.godziny.api.usuario.UsuarioRecuperarDto;
import com.cefet.godziny.domain.casouso.usuario.AtualizarUsuarioCasoUso;
import com.cefet.godziny.domain.casouso.usuario.CriarUsuarioCasoUso;
import com.cefet.godziny.domain.casouso.usuario.ListarUsuarioCasoUso;
import com.cefet.godziny.domain.casouso.usuario.PesquisarUsuarioCasoUso;
import com.cefet.godziny.domain.casouso.usuario.RemoverUsuarioCasoUso;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpa;
import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UsuarioControle implements IUsuarioApi{

    private final UsuarioRepositorioJpa usuarioRepositorioJpa;

    private final CursoRepositorioJpa cursoRepositorioJpa;

    private final AtividadeRepositorioJpa atividadeRepositorioJpa;
    
    private final PasswordEncoder enconder;

    @Override
    public ResponseEntity<UsuarioRecuperarDto> getUsuario(Integer matricula) throws Exception {
        ListarUsuarioCasoUso casoUso = new ListarUsuarioCasoUso(usuarioRepositorioJpa, matricula);
        return ResponseEntity.status(HttpStatus.OK).body(casoUso.validarListagem());
    }

    @Override
    public ResponseEntity<Page<UsuarioRecuperarDto>> pesquisarUsuarios(Pageable pageable, UsuarioFiltroDto usuarioFiltroDto) throws Exception {
        PesquisarUsuarioCasoUso casoUso = new PesquisarUsuarioCasoUso(usuarioRepositorioJpa, cursoRepositorioJpa, usuarioFiltroDto.getMatricula(), usuarioFiltroDto.getCursoSigla(), usuarioFiltroDto.getNome());
        casoUso.validarPesquisa();
        return ResponseEntity.status(HttpStatus.OK).body(casoUso.pesquisarUsuarios(pageable));
    }

    @Override
    public ResponseEntity<Integer> createUsuario(@Valid UsuarioDto dto) throws Exception{
        CriarUsuarioCasoUso casoUso = UsuarioRestConverter.DtoToCriarUsuarioCasoUso(dto, usuarioRepositorioJpa, cursoRepositorioJpa);
        casoUso.validarCriacao();
        dto.setSenha(enconder.encode(dto.getSenha()));
        return ResponseEntity.status(HttpStatus.CREATED).body(casoUso.createUsuario(dto));
    }

    @Override
    public ResponseEntity<Integer> updateUsuario(Integer matricula, @Valid UsuarioDto dto) throws Exception {
        AtualizarUsuarioCasoUso casoUso = UsuarioRestConverter.DtoToUpdateCursoCasoUso(dto, usuarioRepositorioJpa, cursoRepositorioJpa);
        casoUso.validarAtualizacao();
        dto.setSenha(enconder.encode(dto.getSenha()));
        return ResponseEntity.status(HttpStatus.OK).body(casoUso.AtualizarUsuario(dto));
    }

    @Override
    @Transactional
    public ResponseEntity<Void> removeUsuario(Integer matricula) throws Exception {
        RemoverUsuarioCasoUso casoUso = new RemoverUsuarioCasoUso(matricula, usuarioRepositorioJpa, atividadeRepositorioJpa);
        casoUso.validarRemocao();
        casoUso.removerUsuario();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
