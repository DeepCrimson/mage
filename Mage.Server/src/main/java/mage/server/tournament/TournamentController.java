package mage.server.tournament;

import mage.cards.decks.Deck;
import mage.constants.TableState;
import mage.server.managers.ManagerFactory;
import mage.view.TournamentView;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class TournamentController {

    private final ManagerFactory managerFactory;
    private final UUID chatId;
    private final UUID tableId;
    private final ConcurrentMap<UUID, TournamentSession> tournamentSessions = new ConcurrentHashMap<>();
    private boolean started = false;
    private ConcurrentMap<UUID, UUID> userPlayerMap = new ConcurrentHashMap<>();

    public TournamentController(ManagerFactory managerFactory, ConcurrentMap<UUID, UUID> userPlayerMap, UUID tableId) {
        this.managerFactory = managerFactory;
        this.userPlayerMap = userPlayerMap;
        chatId = managerFactory.chatManager().createChatSession("Tournament");
        this.tableId = tableId;
        init();
    }

    private void init() {
    }

    public synchronized void join(UUID userId) {
    }

    public void rejoin(UUID playerId) {
        TournamentSession tournamentSession = tournamentSessions.get(playerId);
        if (tournamentSession == null) {
            return;
        }
        if (!tournamentSession.init()) {
            return;
        }
        tournamentSession.update();
    }

    private void checkStart() {
        if (!started && allJoined()) {
            managerFactory.threadExecutor().getCallExecutor().execute(this::startTournament);
        }
    }

    private boolean allJoined() {
        return true;
    }

    private synchronized void startTournament() {
    }

    private void construct() {
        managerFactory.tableManager().construct(tableId);
    }

    private void initTournament() {
        if (managerFactory.tableManager().getTable(tableId).getState() != TableState.DUELING) {
            managerFactory.tableManager().initTournament(tableId);
        }
    }

    public void submitDeck(UUID playerId, Deck deck) {
    }

    public boolean updateDeck(UUID playerId, Deck deck) {
        if (tournamentSessions.containsKey(playerId)) {
            return tournamentSessions.get(playerId).updateDeck(deck);
        }
        return false;
    }

    public void timeout(UUID userId) {
    }

    public UUID getChatId() {
        return chatId;
    }

    public void quit(UUID userId) {
    }

    private Optional<UUID> getPlayerUserId(UUID playerId) {
        return userPlayerMap.entrySet().stream().filter(entry -> entry.getValue().equals(playerId)).map(Entry::getKey).findFirst();
    }

    public TournamentView getTournamentView() {
        return null;
    }

    public void cleanUpOnRemoveTournament() {
        managerFactory.chatManager().destroyChatSession(chatId);
    }
}
