package video.to.image.auth_ms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AuthMsApplication {


	public static void main(String[] args) {
		SpringApplication.run(AuthMsApplication.class, args);
	}

}
