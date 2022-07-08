package mage.cards.repository;

import mage.game.events.Listener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author North, JayDi85
 */
public enum ExpansionRepository {

    instance;

    private static final String JDBC_URL = "jdbc:h2:file:./db/cards.h2;AUTO_SERVER=TRUE";
    private static final String VERSION_ENTITY_NAME = "expansion";
    private static final long EXPANSION_DB_VERSION = 5;
    private static final long EXPANSION_CONTENT_VERSION = 18;
    private RepositoryEventSource eventSource = new RepositoryEventSource();

    ExpansionRepository() {
    }

    public void subscribe(Listener<RepositoryEvent> listener) {
        eventSource.addListener(listener);
    }

    public void saveSets(final List<ExpansionInfo> newSets, final List<ExpansionInfo> updatedSets, long newContentVersion) {
    }

    public List<String> getSetCodes() {
        return null;
    }

    public List<ExpansionInfo> getSetsWithBasicLandsByReleaseDate() {
        return new LinkedList<>();
    }

    public List<ExpansionInfo> getSetsFromBlock(String blockName) {
        return new LinkedList<>();
    }

    public ExpansionInfo getSetByCode(String setCode) {
        return null;
    }

    public ExpansionInfo getSetByName(String setName) {
        return null;
    }

    public List<ExpansionInfo> getAll() {
        return Collections.emptyList();
    }

    public long getContentVersionConstant() {
        return EXPANSION_CONTENT_VERSION;
    }
}
