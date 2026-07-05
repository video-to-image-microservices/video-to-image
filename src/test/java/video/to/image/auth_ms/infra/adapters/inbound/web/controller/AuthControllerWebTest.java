package video.to.image.auth_ms.infra.adapters.inbound.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import video.to.image.auth_ms.core.domain.exceptions.UnauthorizedException;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.authcontroller.login.AuthLoginRequestDto;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.authcontroller.login.AuthLoginResponseDto;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.create.UserCreateResponseDto;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.exceptions.HttpExceptionHandler;
import video.to.image.auth_ms.infra.adapters.inbound.web.security.JwtAuthenticationEntryPoint;
import video.to.image.auth_ms.infra.adapters.inbound.web.security.JwtAuthenticationFilter;
import video.to.image.auth_ms.infra.adapters.inbound.web.services.AuthService;
import video.to.image.auth_ms.infra.config.JwtProperties;
import video.to.image.auth_ms.infra.config.SecurityConfig;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, HttpExceptionHandler.class})
@EnableConfigurationProperties(JwtProperties.class)
@TestPropertySource(properties = {
        "jwt.secret=test-secret-key-with-at-least-32-characters",
        "jwt.expiration-ms=86400000",
        "spring.cache.type=simple"
})
class AuthControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CacheManager cacheManager;

    @Test
    void login_shouldReturn200_whenCredentialsAreValid() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthLoginResponseDto response = AuthLoginResponseDto.builder()
                .token("jwt-token")
                .user(UserCreateResponseDto.builder()
                        .id(userId)
                        .name("João")
                        .email("joao@email.com")
                        .build())
                .build();

        when(authService.login(any())).thenReturn(response);

        AuthLoginRequestDto request = AuthLoginRequestDto.builder()
                .email("joao@email.com")
                .password("senha123")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.id").value(userId.toString()))
                .andExpect(jsonPath("$.user.email").value("joao@email.com"));
    }

    @Test
    void login_shouldReturn400_whenEmailIsInvalid() throws Exception {
        AuthLoginRequestDto request = AuthLoginRequestDto.builder()
                .email("invalid-email")
                .password("senha123")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void login_shouldReturn401_whenCredentialsAreInvalid() throws Exception {
        when(authService.login(any())).thenThrow(new UnauthorizedException("Credenciais inválidas"));

        AuthLoginRequestDto request = AuthLoginRequestDto.builder()
                .email("joao@email.com")
                .password("wrong-password")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
