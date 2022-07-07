package mage.cards.repository;

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

    public static boolean isDatabaseEmpty() {
        return ExpansionRepository.instance.getSetByCode("GRN") == null
                || CardRepository.instance.findCard("Island") == null;
    }

}
