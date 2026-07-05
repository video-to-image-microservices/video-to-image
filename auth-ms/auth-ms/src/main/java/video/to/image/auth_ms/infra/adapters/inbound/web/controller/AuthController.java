package video.to.image.auth_ms.infra.adapters.inbound.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.authcontroller.login.AuthLoginRequestDto;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.authcontroller.login.AuthLoginResponseDto;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.exceptions.HttpExceptionMessage;
import video.to.image.auth_ms.infra.adapters.inbound.web.services.AuthService;

@RestController
@RequestMapping(value = "/auth", produces = {"application/json"})
@Tag(name = "Auth controller", description = "endpoints de autenticação")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Login de usuário",
            method = "POST"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            )),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            )),
            @ApiResponse(responseCode = "500", description = "Erro interno", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            ))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponseDto> login(@RequestBody @Valid AuthLoginRequestDto body) {
        return ResponseEntity.ok(this.authService.login(body));
    }
}
