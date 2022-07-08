package mage.server;

import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;

public class AuthorizedUserRepository {

    private static final String JDBC_URL = "jdbc:h2:file:./db/authorized_user.h2;AUTO_SERVER=TRUE";
    private static final String VERSION_ENTITY_NAME = "authorized_user";
    // raise this if db structure was changed
    private static final long DB_VERSION = 2;
    private static final RandomNumberGenerator rng = new SecureRandomNumberGenerator();

    private static final AuthorizedUserRepository instance;

    static {
        instance = new AuthorizedUserRepository(JDBC_URL);
    }

    public AuthorizedUserRepository(String connectionString) {
    }

    public static AuthorizedUserRepository getInstance() {
        return instance;
    }

    public void add(final String userName, final String password, final String email) {
    }

    public void remove(final String userName) {
    }

    public AuthorizedUser getByName(String userName) {
        return null;
    }

    public void update(AuthorizedUser authorizedUser) {
    }

    public AuthorizedUser getByEmail(String userName) {
        return null;
    }

    public long getDBVersionFromDB() {
        return 0;
    }

    public boolean checkAlterAndMigrateAuthorizedUser() {
        long currentDBVersion = getDBVersionFromDB();
        if (currentDBVersion == 1 && DB_VERSION == 2) {
            return migrateFrom1To2();
        }
        return true;
    }

    private boolean migrateFrom1To2() {
        return false;
    }
}
