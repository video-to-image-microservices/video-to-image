package video.to.image.auth_ms.infra.migrations;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "001-create-tb-user-indexes", order = "001", author = "auth-ms")
public class UserCollectionChangeUnit {

    @Execution
    public void createIndexes(MongoDatabase db) {
        db.getCollection("tb_user")
                .createIndex(Indexes.ascending("email"), new IndexOptions().unique(true));
    }

    @RollbackExecution
    public void rollbackIndexes(MongoDatabase db) {
        db.getCollection("tb_user").dropIndex(Indexes.ascending("email"));
    }
}
