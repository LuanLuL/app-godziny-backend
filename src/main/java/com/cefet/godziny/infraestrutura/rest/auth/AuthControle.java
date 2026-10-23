package com.cefet.godziny.infraestrutura.rest.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.cefet.godziny.api.auth.AuthDto;
import com.cefet.godziny.api.auth.AuthResponseDto;
import com.cefet.godziny.api.auth.IAuthApi;
import com.cefet.godziny.infraestrutura.persistencia.auth.AuthRepositorioJpa;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AuthControle implements IAuthApi {

    private final AuthRepositorioJpa authRepositorioJpa;

    @Override
    public ResponseEntity<AuthResponseDto> loginUsuario(@Valid AuthDto dto) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(authRepositorioJpa.loginUsuario(dto));
    }
}

