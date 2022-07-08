package mage.cards.repository;
import java.sql.SQLException;

/**
 * @author North, JayDi85
 */
public final class RepositoryUtil {

    public static final boolean CARD_DB_RECREATE_BY_CLIENT_SIDE = true; // re-creates db from client (best performance) or downloads from server on connects (can be slow)

    public static void bootstrapLocalDb() {
        // call local db to init all sets and cards repository (need for correct updates cycle, not on random request)
        ExpansionRepository.instance.getContentVersionConstant();
        CardRepository.instance.getContentVersionConstant();
    }

    public static boolean isDatabaseObsolete(String entityName, long version) throws SQLException {
return false;
    }

    public static boolean isNewBuildRun(String entityName, Class clazz) throws SQLException {
return false;
    }

    public static void updateVersion(String entityName, long version) throws SQLException {
    }

    public static long getDatabaseVersion(String entityName) throws SQLException {
return 0;
    }

}
