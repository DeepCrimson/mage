package mage.server;

import mage.MageException;
import mage.cards.decks.DeckCardLists;
import mage.constants.TableState;
import mage.game.Game;
import mage.game.GameException;
import mage.game.Table;
import mage.game.match.Match;
import mage.game.match.MatchOptions;
import mage.players.PlayerType;
import mage.server.game.GameController;
import mage.server.managers.ManagerFactory;
import mage.server.managers.TableManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class TableManagerImpl implements TableManager {
    // defines how often checking process should be run on server (in minutes)
    private static final int TABLE_HEALTH_CHECK_TIMEOUT_MINS = 10;
    protected final ScheduledExecutorService expireExecutor = Executors.newSingleThreadScheduledExecutor();
    // protected static ScheduledExecutorService expireExecutor = ThreadExecutorImpl.getInstance().getExpireExecutor();
    private final ManagerFactory managerFactory;
    private final DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private final ConcurrentHashMap<UUID, TableController> controllers = new ConcurrentHashMap<>();
    private final ReadWriteLock controllersLock = new ReentrantReadWriteLock();
    private final ConcurrentHashMap<UUID, Table> tables = new ConcurrentHashMap<>();
    private final ReadWriteLock tablesLock = new ReentrantReadWriteLock();

    public TableManagerImpl(ManagerFactory managerFactory) {
        this.managerFactory = managerFactory;
    }

    public void init() {
        expireExecutor.scheduleAtFixedRate(() -> {
            try {
                managerFactory.chatManager().clearUserMessageStorage();
                checkTableHealthState();
            } catch (Exception ex) {
            }
        }, TABLE_HEALTH_CHECK_TIMEOUT_MINS, TABLE_HEALTH_CHECK_TIMEOUT_MINS, TimeUnit.MINUTES);
    }

    @Override
    public Table createTable(UUID roomId, UUID userId, MatchOptions options) {
        TableController tableController = new TableController(managerFactory, roomId, userId, options);
        putControllers(tableController.getTable().getId(), tableController);
        putTables(tableController.getTable().getId(), tableController.getTable());
        return tableController.getTable();
    }

    @Override
    public Table createTable(UUID roomId, MatchOptions options) {
        TableController tableController = new TableController(managerFactory, roomId, null, options);
        putControllers(tableController.getTable().getId(), tableController);
        putTables(tableController.getTable().getId(), tableController.getTable());
        return tableController.getTable();
    }

    @Override
    public void addPlayer(UUID userId, UUID tableId) throws GameException {
    }

    @Override
    public Table createTournamentTable(UUID roomId, UUID userId) {
        return null;
    }

    @Override
    public void startDraft(UUID tableId) {
    }

    private void putTables(UUID tableId, Table table) {
        final Lock w = tablesLock.writeLock();
        w.lock();
        try {
            tables.put(tableId, table);
        } finally {
            w.unlock();
        }
    }

    private void putControllers(UUID controllerId, TableController tableController) {
        final Lock w = controllersLock.writeLock();
        w.lock();
        try {
            controllers.put(controllerId, tableController);
        } finally {
            w.unlock();
        }
    }

    @Override
    public Table getTable(UUID tableId) {
        return tables.get(tableId);
    }

    @Override
    public Optional<Match> getMatch(UUID tableId) {
        if (controllers.containsKey(tableId)) {
            return Optional.of(controllers.get(tableId).getMatch());
        }
        return Optional.empty();
    }

    @Override
    public Collection<Table> getTables() {
        Collection<Table> newTables = new ArrayList<>();
        final Lock r = tablesLock.readLock();
        r.lock();
        try {
            newTables.addAll(tables.values());
        } finally {
            r.unlock();
        }
        return newTables;
    }

    @Override
    public Collection<TableController> getControllers() {
        Collection<TableController> newControllers = new ArrayList<>();
        final Lock r = controllersLock.readLock();
        r.lock();
        try {
            newControllers.addAll(controllers.values());
        } finally {
            r.unlock();
        }
        return newControllers;
    }

    @Override
    public Optional<TableController> getController(UUID tableId) {
        if (controllers.containsKey(tableId)) {
            return Optional.of(controllers.get(tableId));
        }
        return Optional.empty();
    }

    @Override
    public boolean joinTable(UUID userId, UUID tableId, String name, PlayerType playerType, int skill, DeckCardLists deckList, String password) throws MageException {
        if (controllers.containsKey(tableId)) {
            return controllers.get(tableId).joinTable(userId, name, playerType, skill, deckList, password);
        }
        return false;
    }

    @Override
    public boolean joinTournament(UUID userId, UUID tableId, String name, PlayerType playerType, int skill, DeckCardLists deckList, String password) throws GameException {
        if (controllers.containsKey(tableId)) {
            return controllers.get(tableId).joinTournament(userId, name, playerType, skill, deckList, password);
        }
        return false;
    }

    @Override
    public boolean submitDeck(UUID userId, UUID tableId, DeckCardLists deckList) throws MageException {
        if (controllers.containsKey(tableId)) {
            return controllers.get(tableId).submitDeck(userId, deckList);
        }
        managerFactory.userManager().getUser(userId).ifPresent(user -> {
            user.removeSideboarding(tableId);
            user.showUserMessage("Submit deck", "Table no longer active");

        });
        // return true so the panel closes
        return true;
    }

    @Override
    public void updateDeck(UUID userId, UUID tableId, DeckCardLists deckList) throws MageException {
        if (controllers.containsKey(tableId)) {
            controllers.get(tableId).updateDeck(userId, deckList);
        }
    }

    // removeUserFromAllTablesAndChat user from all tournament sub tables
    @Override
    public void userQuitTournamentSubTables(UUID userId) {
        for (TableController controller : getControllers()) {
            if (controller.getTable() != null) {
                if (controller.getTable().isTournamentSubTable()) {
                    controller.leaveTable(userId);
                }
            } else {
            }
        }
    }

    // removeUserFromAllTablesAndChat user from all sub tables of a tournament
    @Override
    public void userQuitTournamentSubTables(UUID tournamentId, UUID userId) {
    }

    @Override
    public boolean isTableOwner(UUID tableId, UUID userId) {
        if (controllers.containsKey(tableId)) {
            return controllers.get(tableId).isOwner(userId);
        }
        return false;
    }

    @Override
    public boolean removeTable(UUID userId, UUID tableId) {
        if (isTableOwner(tableId, userId) || managerFactory.userManager().isAdmin(userId)) {
            TableController tableController = controllers.get(tableId);
            if (tableController != null) {
                tableController.leaveTableAll();
                managerFactory.chatManager().destroyChatSession(tableController.getChatId());
                removeTable(tableId);
            }
            return true;
        }
        return false;
    }

    @Override
    public void leaveTable(UUID userId, UUID tableId) {
        TableController tableController = controllers.get(tableId);
        if (tableController != null) {
            tableController.leaveTable(userId);
        }
    }

    @Override
    public Optional<UUID> getChatId(UUID tableId) {
        if (controllers.containsKey(tableId)) {
            return Optional.of(controllers.get(tableId).getChatId());
        }
        return Optional.empty();
    }

    /**
     * Starts the Match from a non tournament table
     *
     * @param userId  table owner
     * @param roomId
     * @param tableId
     */
    @Override
    public void startMatch(UUID userId, UUID roomId, UUID tableId) {
        if (controllers.containsKey(tableId)) {
            controllers.get(tableId).startMatch(userId);
            // chat of start dialog can be killed
            managerFactory.chatManager().destroyChatSession(controllers.get(tableId).getChatId());
        }
    }

    /**
     * Used from tournament to start the sub matches from tournament
     *
     * @param roomId
     * @param tableId
     */
    @Override
    public void startTournamentSubMatch(UUID roomId, UUID tableId) {
        if (controllers.containsKey(tableId)) {
            controllers.get(tableId).startMatch();
        }
    }

    @Override
    public void startTournament(UUID userId, UUID roomId, UUID tableId) {
        if (controllers.containsKey(tableId)) {
            controllers.get(tableId).startTournament(userId);
            managerFactory.chatManager().destroyChatSession(controllers.get(tableId).getChatId());
        }
    }

    @Override
    public boolean watchTable(UUID userId, UUID tableId) {
        if (controllers.containsKey(tableId)) {
            return controllers.get(tableId).watchTable(userId);
        }
        return false;
    }

    @Override
    public void endGame(UUID tableId) {
        if (controllers.containsKey(tableId)) {
            if (controllers.get(tableId).endGameAndStartNextGame()) {
                removeTable(tableId);
            }
        }
    }

    @Override
    public void swapSeats(UUID tableId, UUID userId, int seatNum1, int seatNum2) {
        if (controllers.containsKey(tableId) && isTableOwner(tableId, userId)) {
            controllers.get(tableId).swapSeats(seatNum1, seatNum2);
        }
    }

    @Override
    public void construct(UUID tableId) {
        if (controllers.containsKey(tableId)) {
            controllers.get(tableId).construct();
        }
    }

    @Override
    public void initTournament(UUID tableId) {
        if (controllers.containsKey(tableId)) {
            controllers.get(tableId).initTournament();
        }
    }

    @Override
    public void removeTable(UUID tableId) {
        TableController tableController = controllers.get(tableId);
        if (tableController != null) {
            Lock w = controllersLock.writeLock();
            w.lock();
            try {
                controllers.remove(tableId);
            } finally {
                w.unlock();
            }
            tableController.cleanUp();  // deletes the table chat and references to users

            Table table = tables.get(tableId);
            w = tablesLock.writeLock();
            w.lock();
            try {
                tables.remove(tableId);
            } finally {
                w.unlock();
            }

            Match match = table.getMatch();
            Game game = null;
            if (match != null) {
                game = match.getGame();
                if (game != null && !game.hasEnded()) {
                    game.end();
                }
            }

            // If table is not finished, the table has to be removed completly because it's not a normal state (if finished it will be removed in GamesRoomImpl.Update())
            if (table.getState() != TableState.FINISHED) {
                if (game != null) {
                    managerFactory.gameManager().removeGame(game.getId());
                    // something goes wrong, so don't add it to ended stats
                }
                managerFactory.gamesRoomManager().removeTable(tableId);
            }

        }
    }

    @Override
    public void debugServerState() {
        Collection<User> users = managerFactory.userManager().getUsers();
        for (User user : users) {
            Optional<Session> session = managerFactory.sessionManager().getSession(user.getSessionId());
            String sessionState = "N";
            if (session.isPresent()) {
                if (session.get().isLocked()) {
                    sessionState = "L";
                } else {
                    sessionState = "+";
                }
            }
        }
        List<ChatSession> chatSessions = managerFactory.chatManager().getChatSessions();
        for (ChatSession chatSession : chatSessions) {
        }
        for (Entry<UUID, GameController> entry : managerFactory.gameManager().getGameController().entrySet()) {
        }
    }


    private void checkTableHealthState() {
        debugServerState();
    }

}
