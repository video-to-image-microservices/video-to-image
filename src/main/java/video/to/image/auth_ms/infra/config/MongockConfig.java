package video.to.image.auth_ms.infra.config;

import com.mongodb.client.MongoClient;
import io.mongock.driver.mongodb.sync.v4.driver.MongoSync4Driver;
import io.mongock.runner.standalone.MongockStandalone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "mongock.enabled", havingValue = "true", matchIfMissing = true)
public class MongockConfig {

    @Bean
    ApplicationRunner mongockRunner(
            MongoClient mongoClient,
            @Value("${spring.mongodb.database}") String databaseName
    ) {
        return (ApplicationArguments args) -> MongockStandalone.builder()
                .setDriver(MongoSync4Driver.withDefaultLock(mongoClient, databaseName))
                .addMigrationScanPackage("video.to.image.auth_ms.infra.migrations")
                .setTransactional(false)
                .buildRunner()
                .execute();
    }
}
