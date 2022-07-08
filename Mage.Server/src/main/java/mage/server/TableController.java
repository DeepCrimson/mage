package mage.server;

import mage.MageException;
import mage.cards.decks.Deck;
import mage.cards.decks.DeckCardLists;
import mage.constants.RangeOfInfluence;
import mage.constants.TableState;
import mage.game.*;
import mage.game.match.Match;
import mage.game.match.MatchOptions;
import mage.game.match.MatchPlayer;
import mage.game.tournament.TournamentOptions;
import mage.players.Player;
import mage.players.PlayerType;
import mage.server.game.GameFactory;
import mage.server.game.PlayerFactory;
import mage.server.managers.ManagerFactory;
import mage.server.util.ServerMessagesUtil;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class TableController {
    private final ManagerFactory managerFactory;
    private final UUID userId;
    private final UUID chatId;
    private final String controllerName;
    private final Table table;
    private final ConcurrentHashMap<UUID, UUID> userPlayerMap = new ConcurrentHashMap<>(); // human players only, use table.seats for AI
    private Match match;
    private MatchOptions options;
    private ScheduledFuture<?> futureTimeout;
    protected final ScheduledExecutorService timeoutExecutor;

    public TableController(ManagerFactory managerFactory, UUID roomId, UUID userId, MatchOptions options) {
        this.managerFactory = managerFactory;
        timeoutExecutor = managerFactory.threadExecutor().getTimeoutExecutor();
        this.userId = userId;
        this.options = options;
        match = GameFactory.instance.createMatch(options.getGameType(), options);
        if (userId != null) {
            Optional<User> user = managerFactory.userManager().getUser(userId);
            // TODO: Handle if user == null
            controllerName = user.map(User::getName).orElse("undefined");
        } else {
            controllerName = "System";
        }
table = null;
        chatId = managerFactory.chatManager().createChatSession("Match Table " + table.getId());
        init();
    }

    public TableController(ManagerFactory managerFactory, UUID roomId, UUID userId, TournamentOptions options) {
        this.managerFactory = managerFactory;
        this.timeoutExecutor = managerFactory.threadExecutor().getTimeoutExecutor();
        this.userId = userId;
        if (userId != null) {
            Optional<User> user = managerFactory.userManager().getUser(userId);
            if (!user.isPresent()) {
                controllerName = "[unknown]";
            } else {
                controllerName = user.get().getName();
            }
        } else {
            controllerName = "System";
        }
        chatId = null;
        table = null;
    }

    private void init() {
    }

    public synchronized boolean joinTournament(UUID userId, String name, PlayerType playerType, int skill, DeckCardLists deckList, String password) throws GameException {
        return false;
    }

    public boolean hasPlayer(UUID userId) {
        return userPlayerMap.containsKey(userId);
    }

    public synchronized boolean replaceDraftPlayer(Player oldPlayer, String name, PlayerType playerType, int skill) {
        return true;
    }

    public synchronized boolean joinTable(UUID userId, String name, PlayerType playerType, int skill, DeckCardLists deckList, String password) throws MageException {
        Optional<User> _user = managerFactory.userManager().getUser(userId);
        if (!_user.isPresent()) {
            return false;
        }
        User user = _user.get();
        if (userPlayerMap.containsKey(userId) && playerType == PlayerType.HUMAN) {
            user.showUserMessage("Join Table", new StringBuilder("You can join a table only one time.").toString());
            return false;
        }
        if (table.getState() != TableState.WAITING) {
            user.showUserMessage("Join Table", "No available seats.");
            return false;
        }
        // check password
        if (!table.getMatch().getOptions().getPassword().isEmpty() && playerType == PlayerType.HUMAN) {
            if (!table.getMatch().getOptions().getPassword().equals(password)) {
                user.showUserMessage("Join Table", "Wrong password.");
                return false;
            }
        }
        Seat seat = table.getNextAvailableSeat(playerType);
        if (seat == null) {
            user.showUserMessage("Join Table", "No available seats.");
            return false;
        }
        Deck deck = Deck.load(deckList, false, false);

        // Check quit ratio.
        int quitRatio = table.getMatch().getOptions().getQuitRatio();
        if (quitRatio < user.getMatchQuitRatio()) {
            String message = new StringBuilder("Your quit ratio ").append(user.getMatchQuitRatio())
                    .append("% is higher than the table requirement ").append(quitRatio).append('%').toString();
            user.showUserMessage("Join Table", message);
            return false;
        }

        // Check minimum rating.
        int minimumRating = table.getMatch().getOptions().getMinimumRating();
        int userRating;
        if (table.getMatch().getOptions().isLimited()) {
            userRating = user.getUserData().getLimitedRating();
        } else {
            userRating = user.getUserData().getConstructedRating();
        }
        if (userRating < minimumRating) {
            String message = new StringBuilder("Your rating ").append(userRating)
                    .append(" is lower than the table requirement ").append(minimumRating).toString();
            user.showUserMessage("Join Table", message);
            return false;
        }

        // Check power level for table (currently only used for EDH/Commander table)
        int edhPowerLevel = table.getMatch().getOptions().getEdhPowerLevel();
        if (edhPowerLevel > 0 && table.getValidator().getName().toLowerCase(Locale.ENGLISH).equals("commander")) {
            int deckEdhPowerLevel = table.getValidator().getEdhPowerLevel(deck);
            if (deckEdhPowerLevel % 100 > edhPowerLevel) {
                String message = new StringBuilder("Your deck appears to be too powerful for this table.\n\nReduce the number of extra turn cards, infect, counters, fogs, reconsider your commander. ")
                        .append("\nThe table requirement has a maximum power level of ").append(edhPowerLevel).append(" whilst your deck has a calculated power level of ")
                        .append(deckEdhPowerLevel % 100).toString();
                user.showUserMessage("Join Table", message);
                return false;
            }

            boolean restrictedColor = false;
            String badColor = "";
            int colorVal = edhPowerLevel % 10;
            if (colorVal == 6 && deckEdhPowerLevel >= 10000000) {
                restrictedColor = true;
                badColor = "white";
            }
            if (colorVal == 4 && deckEdhPowerLevel % 10000000 >= 1000000) {
                restrictedColor = true;
                badColor = "blue";
            }
            if (colorVal == 3 && deckEdhPowerLevel % 1000000 >= 100000) {
                restrictedColor = true;
                badColor = "black";
            }
            if (colorVal == 2 && deckEdhPowerLevel % 100000 >= 10000) {
                restrictedColor = true;
                badColor = "red";
            }
            if (colorVal == 1 && deckEdhPowerLevel % 10000 >= 1000) {
                restrictedColor = true;
                badColor = "green";
            }
            if (restrictedColor) {
                String message = new StringBuilder("Your deck contains ")
                        .append(badColor)
                        .append(".  The creator of the table has requested no ")
                        .append(badColor)
                        .append(" cards to be on the table!").toString();
                user.showUserMessage("Join Table", message);
                return false;
            }
        }

        Optional<Player> playerOpt = createPlayer(name, seat.getPlayerType(), skill);
        if (!playerOpt.isPresent()) {
            String message = "Could not create player " + name + " of type " + seat.getPlayerType();
            user.showUserMessage("Join Table", message);
            return false;
        }
        Player player = playerOpt.get();
        match.addPlayer(player, deck);
        table.joinTable(player, seat);
        //only inform human players and add them to sessionPlayerMap
        if (seat.getPlayer().isHuman()) {
            seat.getPlayer().setUserData(user.getUserData());
            if (!table.isTournamentSubTable()) {
                user.addTable(player.getId(), table);
            }
            user.ccJoinedTable(table.getRoomId(), table.getId(), false);
            userPlayerMap.put(userId, player.getId());
        }
        return true;
    }

    public void addPlayer(UUID userId, Player player, PlayerType playerType, Deck deck) throws GameException {
        if (table.getState() != TableState.WAITING) {
            return;
        }
        Seat seat = table.getNextAvailableSeat(playerType);
        if (seat == null) {
            throw new GameException("No available seats.");
        }
        match.addPlayer(player, deck);
        table.joinTable(player, seat);
        if (player.isHuman()) {
            userPlayerMap.put(userId, player.getId());
        }
    }

    public synchronized boolean submitDeck(UUID userId, DeckCardLists deckList) throws MageException {
        return true;
    }

    public void updateDeck(UUID userId, DeckCardLists deckList) throws MageException {
        boolean validDeck;
        UUID playerId = userPlayerMap.get(userId);
        if (table.getState() != TableState.SIDEBOARDING && table.getState() != TableState.CONSTRUCTING) {
            return;
        }
        Deck deck = Deck.load(deckList, false, false);
        validDeck = updateDeck(userId, playerId, deck);
        if (!validDeck && getTableState() == TableState.SIDEBOARDING) {
        }
    }

    private void submitDeck(UUID userId, UUID playerId, Deck deck) {
        if (table.getState() == TableState.SIDEBOARDING) {
            match.submitDeck(playerId, deck);
            managerFactory.userManager().getUser(userId).ifPresent(user -> user.removeSideboarding(table.getId()));
        } else {
            managerFactory.userManager().getUser(userId).ifPresent(user -> user.removeConstructing(playerId));
        }
    }

    private boolean updateDeck(UUID userId, UUID playerId, Deck deck) {
return true;
    }

    public boolean watchTable(UUID userId) {
return true;
    }

    private Optional<Player> createPlayer(String name, PlayerType playerType, int skill) {
        Optional<Player> playerOpt;
        if (options == null) {
            playerOpt = PlayerFactory.instance.createPlayer(playerType, name, RangeOfInfluence.ALL, skill);
        } else {
            playerOpt = PlayerFactory.instance.createPlayer(playerType, name, options.getRange(), skill);
        }
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
        }
        return playerOpt;
    }

    public void leaveTableAll() {
        for (UUID leavingUserId : userPlayerMap.keySet()) {
            leaveTable(leavingUserId);
        }
        closeTable();
    }

    public synchronized void leaveTable(UUID userId) {
    }

    /**
     * Used from non tournament match to start
     *
     * @param userId owner of the tabel
     */
    public synchronized void startMatch(UUID userId) {
        if (isOwner(userId)) {
            startMatch();
        }
    }

    public synchronized void startMatch() {
        if (table.getState() == TableState.STARTING) {
            try {
                if (table.isTournamentSubTable()) {
                } else {
                    managerFactory.userManager().getUser(userId).ifPresent(user -> {
                    });
                }
                match.startMatch();
                startGame(null);
            } catch (GameException ex) {
                match.endGame();
            }
        }
    }

    private void startGame(UUID choosingPlayerId) throws GameException {
        try {
            match.startGame();
            table.initGame();
            GameOptions gameOptions = new GameOptions();
            gameOptions.rollbackTurnsAllowed = match.getOptions().isRollbackTurnsAllowed();
            gameOptions.bannedUsers = match.getOptions().getBannedUsers();
            gameOptions.planeChase = match.getOptions().isPlaneChase();
            match.getGame().setGameOptions(gameOptions);
            managerFactory.gameManager().createGameSession(match.getGame(), userPlayerMap, table.getId(), choosingPlayerId, gameOptions);
            String creator = null;
            StringBuilder opponent = new StringBuilder();
            for (Entry<UUID, UUID> entry : userPlayerMap.entrySet()) { // do only for no AI players
                if (match.getPlayer(entry.getValue()) != null && !match.getPlayer(entry.getValue()).hasQuit()) {
                    Optional<User> _user = managerFactory.userManager().getUser(entry.getKey());
                    if (_user.isPresent()) {
                        User user = _user.get();
                        user.ccGameStarted(match.getGame().getId(), entry.getValue());

                        if (creator == null) {
                            creator = user.getName();
                        } else {
                            if (opponent.length() > 0) {
                                opponent.append(" - ");
                            }
                            opponent.append(user.getName());
                        }
                    } else {
                        MatchPlayer matchPlayer = match.getPlayer(entry.getValue());
                        if (matchPlayer != null && !matchPlayer.hasQuit()) {
                            matchPlayer.setQuit(true);
                        }
                    }
                }
            }
            // Append AI opponents to the log file
            for (MatchPlayer mPlayer : match.getPlayers()) {
                if (!mPlayer.getPlayer().isHuman()) {
                    if (opponent.length() > 0) {
                        opponent.append(" - ");
                    }
                    opponent.append(mPlayer.getName());
                }
            }
            ServerMessagesUtil.instance.incGamesStarted();

            // log about game started
            if (match.getGame() != null) {
            }
        } catch (Exception ex) {
            if (table != null) {
                managerFactory.tableManager().removeTable(table.getId());
            }
            if (match != null) {
                Game game = match.getGame();
                if (game != null) {
                    managerFactory.gameManager().removeGame(game.getId());
                    // game ended by error, so don't add it to ended stats
                }
            }
        }
    }

    public synchronized void startTournament(UUID userId) {
    }

    private void sideboard(UUID playerId, Deck deck) throws MageException {

        for (Entry<UUID, UUID> entry : userPlayerMap.entrySet()) {
            if (entry.getValue().equals(playerId)) {
                Optional<User> user = managerFactory.userManager().getUser(entry.getKey());
                int remaining = (int) futureTimeout.getDelay(TimeUnit.SECONDS);
                user.ifPresent(user1 -> user1.ccSideboard(deck, table.getId(), remaining, options.isLimited()));
                break;
            }
        }
    }

    public int getRemainingTime() {
        return (int) futureTimeout.getDelay(TimeUnit.SECONDS);
    }

    public void construct() {
        table.construct();
    }

    public void initTournament() {
        table.initTournament();
    }

    public MatchOptions getOptions() {
        return options;
    }

    /**
     * Ends the current game and starts if neccessary the next game
     *
     * @return true if table can be closed
     */
    public boolean endGameAndStartNextGame() {
        // get player that chooses who goes first
        Game game = match.getGame();
        if (game == null) {
            return true;
        }
        UUID choosingPlayerId = match.getChooser();
        match.endGame();
        if (managerFactory.configSettings().isSaveGameActivated() && !game.isSimulation()) {
            if (managerFactory.gameManager().saveGame(game.getId())) {
                match.setReplayAvailable(true);
            }
        }
        managerFactory.gameManager().removeGame(game.getId());
        ServerMessagesUtil.instance.incGamesEnded();

        try {
            if (!match.hasEnded()) {
                if (match.getGame() != null && match.getGame().getGameType().isSideboardingAllowed()) {
                    sideboard();
                }
                if (!match.hasEnded()) {
                    startGame(choosingPlayerId);
                } else {
                    closeTable();
                }
            } else {
                closeTable();
            }
        } catch (GameException ex) {
        }
        return match.hasEnded();
    }

    private void sideboard() {
        table.sideboard();
        setupTimeout(Match.SIDEBOARD_TIME);
        match.sideboard();
        cancelTimeout();
    }

    /**
     * Tables of normal matches or tournament sub tables are no longer needed,
     * if the match ends.
     */
    private void closeTable() {
        this.matchEnd();
        table.closeTable();
    }

    private void matchEnd() {
        if (match != null) {
            for (Entry<UUID, UUID> entry : userPlayerMap.entrySet()) {
                MatchPlayer matchPlayer = match.getPlayer(entry.getValue());
                // opponent(s) left during sideboarding
                if (matchPlayer != null) {
                    if (!matchPlayer.hasQuit()) {
                        managerFactory.userManager().getUser(entry.getKey()).ifPresent(user -> {
                            if (table.getState() == TableState.SIDEBOARDING) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("Match [").append(match.getName()).append("] is over. ");
                            }
                        });
                    }
                }
                // free resources no longer needed
                match.cleanUpOnMatchEnd(managerFactory.configSettings().isSaveGameActivated(), table.isTournament());
            }
        }
    }

    private synchronized void setupTimeout(int seconds) {
    }

    private synchronized void cancelTimeout() {
        if (futureTimeout != null) {
            futureTimeout.cancel(false);
        }
    }

    private void autoSideboard() {
        for (MatchPlayer player : match.getPlayers()) {
            if (!player.isDoneSideboarding()) {
                match.submitDeck(player.getPlayer().getId(), player.generateDeck(table.getValidator()));
            }
        }
    }

    public void swapSeats(int seatNum1, int seatNum2) {
        if (table.getState() == TableState.READY_TO_START) {
            if (seatNum1 >= 0 && seatNum2 >= 0 && seatNum1 < table.getSeats().length && seatNum2 < table.getSeats().length) {
                Player swapPlayer = table.getSeats()[seatNum1].getPlayer();
                PlayerType swapType = table.getSeats()[seatNum1].getPlayerType();
                table.getSeats()[seatNum1].setPlayer(table.getSeats()[seatNum2].getPlayer());
                table.getSeats()[seatNum1].setPlayerType(table.getSeats()[seatNum2].getPlayerType());
                table.getSeats()[seatNum2].setPlayer(swapPlayer);
                table.getSeats()[seatNum2].setPlayerType(swapType);
            }
        }
    }

    public boolean isOwner(UUID userId) {
        if (userId == null) {
            return false;
        }
        return userId.equals(this.userId);
    }

    public Table getTable() {
        return table;
    }

    public UUID getChatId() {
        return chatId;
    }

    public Match getMatch() {
        return match;
    }

    public boolean isTournamentStillValid() {
        return false;
    }

    public UUID getUserId(UUID playerId) {
        for (Map.Entry<UUID, UUID> entry : userPlayerMap.entrySet()) {
            if (entry.getValue().equals(playerId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean isUserStillActive(UUID userId) {
        UUID playerId = userPlayerMap.get(userId);
        if (playerId != null) {
            if (match != null) {
                MatchPlayer matchPlayer = match.getPlayer(playerId);
                return matchPlayer != null && !matchPlayer.hasQuit();
            }
        }
        return false;
    }

    public boolean isMatchTableStillValid() {
        // removes active match only, not tourney
        if (table.isTournament()) {
            return true;
        }

        // only started games need to check
        if (Arrays.asList(
                TableState.WAITING,
                TableState.READY_TO_START,
                TableState.STARTING
        ).contains(table.getState())) {
            // waiting in start dialog
            return true;
        }
        if (match != null && !match.isDoneSideboarding()) {
            // waiting sideboard complete
            return true;
        }

        // no games in started match (error in match init code?)
        if (match.getGame() == null) {
            return false; // critical error
        }

        // find player stats
        int validHumanPlayers = 0;
        int validAIPlayers = 0;

        // check humans
        for (Map.Entry<UUID, UUID> userPlayerEntry : userPlayerMap.entrySet()) {
            MatchPlayer matchPlayer = match.getPlayer(userPlayerEntry.getValue());

            // de-synced users and players listst?
            if (matchPlayer == null) {
                continue;
            }

            if (matchPlayer.getPlayer().isHuman()) {
                if (matchPlayer.getPlayer().isInGame()) {
                    Optional<User> user = managerFactory.userManager().getUser(userPlayerEntry.getKey());

                    // user was logout or disconnected from server, but still in the game somehow
                    if (!user.isPresent() || !user.get().isActive()) {
                        return false; // critical error
                    }

                    // user exits on the server and match player has not quit -> player is valid
                    validHumanPlayers++;
                }
            }
        }

        // check AI
        for (MatchPlayer matchPlayer : match.getPlayers()) {
            if (!matchPlayer.getPlayer().isHuman()) {
                if (matchPlayer.getPlayer().isInGame()) {
                    validAIPlayers++;
                }
            }
        }

        // if someone can play 1 vs 1 (e.g. 2+ players) then keep table
        return validAIPlayers + validHumanPlayers >= 2;
    }

    void cleanUp() {
        if (!table.isTournamentSubTable()) {
            for (Map.Entry<UUID, UUID> entry : userPlayerMap.entrySet()) {
                managerFactory.userManager().getUser(entry.getKey()).ifPresent(user
                        -> user.removeTable(entry.getValue()));
            }

        }
        managerFactory.chatManager().destroyChatSession(chatId);
    }

    public synchronized TableState getTableState() {
        return table.getState();
    }

    public synchronized boolean changeTableStateToStarting() {
        if (table.getState() != TableState.READY_TO_START) {
            // tournament is not ready, can't start
            return false;
        }
        if (!table.allSeatsAreOccupied()) {
            return false;
        }
        table.setState(TableState.STARTING);
        return true;
    }
}
