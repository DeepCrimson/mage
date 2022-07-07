package mage.cards.repository;

import mage.cards.CardSetInfo;
import mage.constants.SetType;
import mage.game.events.Listener;
import mage.util.RandomUtil;

import java.io.File;
import java.util.*;

/**
 * @author North, JayDi85
 */
public enum CardRepository {

    instance;


    public static final Set<String> snowLandSetCodes = new HashSet<>(Arrays.asList(
            "CSP",
            "MH1",
            "SLD",
            "ME2",
            "ICE",
            "KHM"
    ));
    private static final String JDBC_URL = "jdbc:h2:file:./db/cards.h2;AUTO_SERVER=TRUE";
    private static final String VERSION_ENTITY_NAME = "card";
    // raise this if db structure was changed
    private static final long CARD_DB_VERSION = 54;
    // raise this if new cards were added to the server
    private static final long CARD_CONTENT_VERSION = 241;
    private final RepositoryEventSource eventSource = new RepositoryEventSource();
    private Set<String> classNames;

    CardRepository() {
        File file = new File("db");
    }

    public static Boolean haveSnowLands(String setCode) {
        return snowLandSetCodes.contains(setCode);
    }

    public void subscribe(Listener<RepositoryEvent> listener) {
        eventSource.addListener(listener);
    }

    public void saveCards(final List<CardInfo> newCards, long newContentVersion) {
    }

    public boolean cardExists(String className) {
        return false;
    }

    public boolean cardExists(CardSetInfo className) {
        return false;
    }

    private void addNewNames(CardInfo card, Set<String> namesList) {
        // require before call: qb.distinct().selectColumns("name", "modalDoubleFacesSecondSideName"...);

        // normal names
        int result = card.getName().indexOf(" // ");
        if (result > 0) {
            namesList.add(card.getName().substring(0, result));
            namesList.add(card.getName().substring(result + 4));
        } else {
            namesList.add(card.getName());
        }

        // additional names from double side cards
        if (card.getSecondSideName() != null && !card.getSecondSideName().isEmpty()) {
            namesList.add(card.getSecondSideName());
        }
        if (card.getModalDoubleFacesSecondSideName() != null && !card.getModalDoubleFacesSecondSideName().isEmpty()) {
            namesList.add(card.getModalDoubleFacesSecondSideName());
        }
        if (card.getFlipCardName() != null && !card.getFlipCardName().isEmpty()) {
            namesList.add(card.getFlipCardName());
        }
    }

    public Set<String> getNames() {
        return new TreeSet<>();
    }

    public Set<String> getNonLandNames() {
        return new TreeSet<>();
    }

    public Set<String> getNonbasicLandNames() {
        return new TreeSet<>();
    }

    public Set<String> getNotBasicLandNames() {
        return new TreeSet<>();
    }

    public Set<String> getCreatureNames() {
        return new TreeSet<>();
    }

    public Set<String> getArtifactNames() {
        return new TreeSet<>();
    }

    public Set<String> getNonLandAndNonCreatureNames() {
        return new TreeSet<>();
    }

    public Set<String> getNonArtifactAndNonLandNames() {
        return new TreeSet<>();
    }

    public CardInfo findCard(String setCode, String cardNumber) {
        return findCard(setCode, cardNumber, true);
    }

    public CardInfo findCard(String setCode, String cardNumber, boolean ignoreNightCards) {
        return null;
    }

    public List<String> getClassNames() {
        return new ArrayList<>();
    }

    public List<CardInfo> getMissingCards(List<String> classNames) {
        return Collections.emptyList();
    }

    public CardInfo findCard(String name) {
        return findCard(name, false);
    }

    /**
     * @param name
     * @param returnAnySet return card from first available set (WARNING, it's a performance optimization for tests,
     *                     don't use it in real games - users must get random set)
     * @return random card with the provided name or null if none is found
     */
    public CardInfo findCard(String name, boolean returnAnySet) {
        List<CardInfo> cards = returnAnySet ? findCards(name, 1) : findCards(name);
        if (!cards.isEmpty()) {
            return cards.get(RandomUtil.nextInt(cards.size()));
        }
        return null;
    }

    public CardInfo findPreferredCoreExpansionCard(String name, boolean caseInsensitive) {
        return findPreferredCoreExpansionCard(name, caseInsensitive, null);
    }

    public CardInfo findPreferredCoreExpansionCard(String name, boolean caseInsensitive, String preferredSetCode) {
        List<CardInfo> cards;
        if (caseInsensitive) {
            cards = findCardsCaseInsensitive(name);
        } else {
            cards = findCards(name);
        }
        return findPreferredOrLatestCard(cards, preferredSetCode);
    }

    public CardInfo findPreferredCoreExpansionCardByClassName(String canonicalClassName, String preferredSetCode) {
        List<CardInfo> cards = findCardsByClass(canonicalClassName);
        return findPreferredOrLatestCard(cards, preferredSetCode);
    }

    private CardInfo findPreferredOrLatestCard(List<CardInfo> cards, String preferredSetCode) {
        if (!cards.isEmpty()) {
            Date lastReleaseDate = null;
            Date lastExpansionDate = null;
            CardInfo cardToUse = null;
            for (CardInfo cardinfo : cards) {
                ExpansionInfo set = ExpansionRepository.instance.getSetByCode(cardinfo.getSetCode());
                if (set != null) {

                    if ((preferredSetCode != null) && (preferredSetCode.equals(set.getCode()))) {
                        return cardinfo;
                    }

                    if (set.getType().isStandardLegal() && (lastExpansionDate == null || set.getReleaseDate().after(lastExpansionDate))) {
                        cardToUse = cardinfo;
                        lastExpansionDate = set.getReleaseDate();
                    }
                    if (lastExpansionDate == null && (lastReleaseDate == null || set.getReleaseDate().after(lastReleaseDate))) {
                        cardToUse = cardinfo;
                        lastReleaseDate = set.getReleaseDate();
                    }
                }
            }
            return cardToUse;
        }
        return null;
    }

    public CardInfo findCardWPreferredSet(String name, String expansion, boolean caseInsensitive) {
        List<CardInfo> cards;
        if (caseInsensitive) {
            cards = findCardsCaseInsensitive(name);
        } else {
            cards = findCards(name);
        }
        if (!cards.isEmpty()) {
            for (CardInfo cardinfo : cards) {
                if (cardinfo.getSetCode() != null && expansion != null && expansion.equalsIgnoreCase(cardinfo.getSetCode())) {
                    return cardinfo;
                }
            }
        }
        return findPreferredCoreExpansionCard(name, true);
    }

    public List<CardInfo> findCards(String name) {
        return findCards(name, 0);
    }

    /**
     * Find card's reprints from all sets
     *
     * @param name
     * @param limitByMaxAmount return max amount of different cards (if 0 then return card from all sets)
     * @return
     */
    public List<CardInfo> findCards(String name, long limitByMaxAmount) {
        return Collections.emptyList();
    }

    public List<CardInfo> findCardsByClass(String canonicalClassName) {
        return Collections.emptyList();
    }

    public List<CardInfo> findCardsCaseInsensitive(String name) {
        return Collections.emptyList();
    }

    /**
     * Warning, don't use db functions in card's code - it generates heavy db loading in AI simulations. If you
     * need that feature then check for simulation mode. See https://github.com/magefree/mage/issues/7014
     *
     * @param criteria
     * @return
     */
    public List<CardInfo> findCards(CardCriteria criteria) {
        return Collections.emptyList();
    }

    public CardInfo findOldestNonPromoVersionCard(String name) {
        return null;
    }

    public long getContentVersionFromDB() {
        return 0;
    }

    public void setContentVersion(long version) {
    }

    public long getContentVersionConstant() {
        return CARD_CONTENT_VERSION;
    }

    public void closeDB() {
    }

    public void openDB() {
    }

    static class OldestNonPromoComparator implements Comparator<CardInfo> {
        @Override
        public int compare(CardInfo a, CardInfo b) {
            ExpansionInfo aSet = ExpansionRepository.instance.getSetByCode(a.getSetCode());
            ExpansionInfo bSet = ExpansionRepository.instance.getSetByCode(b.getSetCode());
            if (aSet.getType() == SetType.PROMOTIONAL && bSet.getType() != SetType.PROMOTIONAL) {
                return 1;
            }
            if (bSet.getType() == SetType.PROMOTIONAL && aSet.getType() != SetType.PROMOTIONAL) {
                return -1;
            }
            if (aSet.getReleaseDate().after(bSet.getReleaseDate())) {
                return 1;
            }
            if (aSet.getReleaseDate().before(bSet.getReleaseDate())) {
                return -1;
            }
            return Integer.compare(a.getCardNumberAsInt(), b.getCardNumberAsInt());
        }
    }
}
