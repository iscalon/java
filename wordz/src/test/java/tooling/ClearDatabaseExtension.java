package tooling;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Profile("!production")
public class ClearDatabaseExtension implements BeforeEachCallback {

    @Override public void beforeEach(ExtensionContext extensionContext) {
        Flyway flyway = SpringExtension.getApplicationContext(extensionContext)
                .getBean(Flyway.class);
        flyway.clean();
        flyway.migrate();
    }
}
