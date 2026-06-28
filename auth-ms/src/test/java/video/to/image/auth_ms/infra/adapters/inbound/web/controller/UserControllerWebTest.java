package video.to.image.auth_ms.infra.adapters.inbound.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import video.to.image.auth_ms.core.domain.entities.User;
import video.to.image.auth_ms.core.domain.enums.ConstMessagesEnum;
import video.to.image.auth_ms.core.domain.exceptions.ForbiddenException;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.create.UserCreateRequestDto;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.create.UserCreateResponseDto;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.update.UserUpdateRequestDto;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.exceptions.HttpExceptionHandler;
import video.to.image.auth_ms.infra.adapters.inbound.web.security.JwtAuthenticationEntryPoint;
import video.to.image.auth_ms.infra.adapters.inbound.web.security.JwtAuthenticationFilter;
import video.to.image.auth_ms.infra.adapters.inbound.web.services.UserService;
import video.to.image.auth_ms.infra.adapters.outbound.security.Hs256JwtTokenGeneratorAdapter;
import video.to.image.auth_ms.infra.config.JwtProperties;
import video.to.image.auth_ms.infra.config.SecurityConfig;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, HttpExceptionHandler.class})
@TestPropertySource(properties = {
        "jwt.secret=test-secret-key-with-at-least-32-characters",
        "jwt.expiration-ms=86400000"
})
class UserControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    private UUID userId;
    private String validToken;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-with-at-least-32-characters");
        jwtProperties.setExpirationMs(86400000);

        Hs256JwtTokenGeneratorAdapter tokenGenerator = new Hs256JwtTokenGeneratorAdapter(jwtProperties);
        User user = new User(userId, "João", "joao@email.com", "hashed");
        validToken = tokenGenerator.generate(user);
    }

    @Test
    void create_shouldReturn201_withoutToken() throws Exception {
        UserCreateResponseDto response = UserCreateResponseDto.builder()
                .id(userId)
                .name("João")
                .email("joao@email.com")
                .build();

        when(userService.create(any())).thenReturn(response);

        UserCreateRequestDto request = UserCreateRequestDto.builder()
                .name("João")
                .email("joao@email.com")
                .password("senha123")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    void get_shouldReturn401_whenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void get_shouldReturn200_whenTokenIsValid() throws Exception {
        UserCreateResponseDto response = UserCreateResponseDto.builder()
                .id(userId)
                .name("João")
                .email("joao@email.com")
                .build();

        when(userService.findById(userId)).thenReturn(response);

        mockMvc.perform(get("/users/{id}", userId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()));
    }

    @Test
    void get_shouldReturn403_whenAccessIsDenied() throws Exception {
        UUID otherUserId = UUID.randomUUID();

        when(userService.findById(otherUserId))
                .thenThrow(new ForbiddenException(ConstMessagesEnum.ACCESS_DENIED.getMessagem()));

        mockMvc.perform(get("/users/{id}", otherUserId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void update_shouldReturn400_whenNameIsEmpty() throws Exception {
        UserUpdateRequestDto request = UserUpdateRequestDto.builder()
                .name("")
                .build();

        mockMvc.perform(put("/users/{id}", userId)
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void delete_shouldReturn204_whenTokenIsValid() throws Exception {
        doNothing().when(userService).delete(eq(userId));

        mockMvc.perform(delete("/users/{id}", userId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNoContent());
    }
}
