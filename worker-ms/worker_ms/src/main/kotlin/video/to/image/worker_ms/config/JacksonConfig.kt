package video.to.image.worker_ms.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
open class JacksonConfig {

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper = ObjectMapper().findAndRegisterModules()
}
