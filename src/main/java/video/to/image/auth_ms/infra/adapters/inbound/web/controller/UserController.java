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
import org.springframework.web.bind.annotation.*;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.create.UserCreateRequestDto;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.create.UserCreateResponseDto;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.update.UserUpdateRequestDto;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.exceptions.HttpExceptionMessage;
import video.to.image.auth_ms.infra.adapters.inbound.web.services.UserService;

import java.util.UUID;

@RestController
@RequestMapping(value = "/users", produces = {"application/json"})
@Tag(name = "User controller", description = "endpoints para gerenciamento de usuários")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @Operation(
            summary = "Criação de usuário",
            method = "POST"
    )
    @ApiResponses( value = {
            @ApiResponse(responseCode = "201", description = "Criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            )),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            )),
            @ApiResponse(responseCode = "500", description = "Erro interno", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            ))
    })
    @PostMapping
    public ResponseEntity<UserCreateResponseDto> create(@RequestBody @Valid UserCreateRequestDto body) {
        return ResponseEntity.status(201).body(this.service.create(body));
    }

    @Operation(
            summary = "Atualização de dados do usuário",
            method = "PUT"
    )
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            )),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            )),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            )),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            )),
            @ApiResponse(responseCode = "409", description = "Conflito de dados", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            )),
            @ApiResponse(responseCode = "500", description = "Erro interno", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            ))
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserCreateResponseDto> update(@PathVariable UUID id, @RequestBody @Valid UserUpdateRequestDto body) {
        return ResponseEntity.ok().body(this.service.update(id, body));
    }

    @Operation(
            summary = "Buscar por usuário",
            method = "GET"
    )
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Encontrado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            )),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            )),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            )),
            @ApiResponse(responseCode = "500", description = "Erro interno", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            ))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserCreateResponseDto> get(@PathVariable UUID id) {
        var user = this.service.findById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "Deleta usuário",
            method = "DELETE"
    )
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Deletado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            )),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            )),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            )),
            @ApiResponse(responseCode = "500", description = "Erro interno", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HttpExceptionMessage.class)
            ))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        this.service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
