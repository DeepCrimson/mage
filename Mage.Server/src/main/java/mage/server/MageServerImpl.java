package mage.server;

import mage.MageException;
import mage.cards.decks.DeckCardLists;
import mage.cards.decks.DeckValidatorFactory;
import mage.cards.repository.CardInfo;
import mage.cards.repository.CardRepository;
import mage.cards.repository.ExpansionInfo;
import mage.cards.repository.ExpansionRepository;
import mage.constants.ManaType;
import mage.constants.PlayerAction;
import mage.constants.TableState;
import mage.game.match.MatchOptions;
import mage.game.tournament.TournamentOptions;
import mage.interfaces.Action;
import mage.interfaces.ActionWithResult;
import mage.interfaces.MageServer;
import mage.interfaces.ServerState;
import mage.players.PlayerType;
import mage.players.net.UserData;
import mage.remote.MageVersionException;
import mage.server.draft.CubeFactory;
import mage.server.game.GameFactory;
import mage.server.game.GamesRoom;
import mage.server.game.PlayerFactory;
import mage.server.managers.ManagerFactory;
import mage.server.services.impl.FeedbackServiceImpl;
import mage.server.util.ServerMessagesUtil;
import mage.server.util.SystemUtil;
import mage.utils.*;
import mage.view.*;

import javax.management.timer.Timer;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @author BetaSteward_at_googlemail.com, noxx
 */
public class MageServerImpl implements MageServer {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final ExecutorService callExecutor;
    private final ManagerFactory managerFactory;
    private final String adminPassword;
    private final boolean testMode;
    private final LinkedHashMap<String, String> activeAuthTokens = new LinkedHashMap<String, String>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            // Keep the latest 1024 auth tokens in memory.
            return size() > 1024;
        }
    };

    public MageServerImpl(ManagerFactory managerFactory, String adminPassword, boolean testMode) {
        this.managerFactory = managerFactory;
        this.adminPassword = adminPassword;
        this.testMode = testMode;
        this.callExecutor = managerFactory.threadExecutor().getCallExecutor();
        ServerMessagesUtil.instance.getMessages();
    }

    // generateAuthToken returns a uniformly distributed 6-digits string.
    static private String generateAuthToken() {
        return String.format("%06d", RANDOM.nextInt(1000000));
    }

    @Override
    public TableView createTournamentTable(String var1, UUID var2, TournamentOptions var3) throws MageException {
        return null;
    }

    @Override
    public boolean registerUser(String sessionId, String userName, String password, String email) throws MageException {
        return managerFactory.sessionManager().registerUser(sessionId, userName, password, email);
    }

    @Override
    public boolean emailAuthToken(String sessionId, String email) throws MageException {
        if (!managerFactory.configSettings().isAuthenticationActivated()) {
            sendErrorMessageToClient(sessionId, Session.REGISTRATION_DISABLED_MESSAGE);
            return false;
        }

        AuthorizedUser authorizedUser = AuthorizedUserRepository.getInstance().getByEmail(email);
        if (authorizedUser == null) {
            sendErrorMessageToClient(sessionId, "No user was found with the email address " + email);
            return false;
        }

        String authToken = generateAuthToken();
        activeAuthTokens.put(email, authToken);
        String subject = "XMage Password Reset Auth Token";
        String text = "Use this auth token to reset " + authorizedUser.name + "'s password: " + authToken + '\n'
                + "It's valid until the next server restart.";
        boolean success;
        if (!managerFactory.configSettings().getMailUser().isEmpty()) {
            success = managerFactory.mailClient().sendMessage(email, subject, text);
        } else {
            success = managerFactory.mailgunClient().sendMessage(email, subject, text);
        }
        if (!success) {
            sendErrorMessageToClient(sessionId, "There was an error inside the server while emailing an auth token");
            return false;
        }
        return true;
    }

    @Override
    public boolean resetPassword(String sessionId, String email, String authToken, String password) throws MageException {
        if (!managerFactory.configSettings().isAuthenticationActivated()) {
            sendErrorMessageToClient(sessionId, Session.REGISTRATION_DISABLED_MESSAGE);
            return false;
        }

        // multi-step reset:
        // - send auth token
        // - check auth token to confirm reset

        String storedAuthToken = activeAuthTokens.get(email);
        if (storedAuthToken == null || !storedAuthToken.equals(authToken)) {
            sendErrorMessageToClient(sessionId, "Invalid auth token");
            return false;
        }

        AuthorizedUser authorizedUser = AuthorizedUserRepository.getInstance().getByEmail(email);
        if (authorizedUser == null) {
            sendErrorMessageToClient(sessionId, "User with that email doesn't exists");
            return false;
        }

        // recreate user with new password
        AuthorizedUserRepository.getInstance().remove(authorizedUser.getName());
        AuthorizedUserRepository.getInstance().add(authorizedUser.getName(), password, email);
        activeAuthTokens.remove(email);
        return true;
    }

    @Override
    public boolean connectUser(String userName, String password, String sessionId, MageVersion version, String userIdStr) throws MageException {
        try {
            if (version.compareTo(Main.getVersion()) != 0) {
                throw new MageVersionException(version, Main.getVersion());
            }
            return managerFactory.sessionManager().connectUser(sessionId, userName, password, userIdStr);
        } catch (MageException ex) {
            if (ex instanceof MageVersionException) {
                throw ex;
            }
            handleException(ex);
        }
        return false;
    }

    @Override
    public boolean setUserData(final String userName, final String sessionId, final UserData userData, final String clientVersion, final String userIdStr) throws MageException {
        return executeWithResult("setUserData", sessionId, new ActionWithBooleanResult() {
            @Override
            public Boolean execute() throws MageException {
                return managerFactory.sessionManager().setUserData(userName, sessionId, userData, clientVersion, userIdStr);
            }
        });
    }

    @Override
    public boolean connectAdmin(String adminPassword, String sessionId, MageVersion version) throws MageException {
        try {
            if (version.compareTo(Main.getVersion()) != 0) {
                throw new MageException("Wrong client version " + version + ", expecting version " + Main.getVersion());
            }
            if (!adminPassword.equals(this.adminPassword)) {
                throw new MageException("Wrong password");
            }
            return managerFactory.sessionManager().connectAdmin(sessionId);
        } catch (Exception ex) {
            handleException(ex);
        }
        return false;
    }

    @Override
    public TableView createTable(final String sessionId, final UUID roomId, final MatchOptions options) throws MageException {
        return executeWithResult("createTable", sessionId, new MyActionWithTableViewResult(sessionId, options, roomId));
    }

    @Override
    public void removeTable(final String sessionId, final UUID roomId, final UUID tableId) throws MageException {
        execute("removeTable", sessionId, () -> {
            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                managerFactory.tableManager().removeTable(userId, tableId);
            });
        });
    }

    @Override
    public boolean joinTable(final String sessionId, final UUID roomId, final UUID tableId, final String name, final PlayerType playerType, final int skill, final DeckCardLists deckList, final String password) throws MageException {
        return executeWithResult("joinTable", sessionId, new ActionWithBooleanResult() {
            @Override
            public Boolean execute() throws MageException {
                Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
                if (!session.isPresent()) {
                    return false;
                }
                UUID userId = session.get().getUserId();
                if (userId == null) {
                    return false;
                }
                Optional<GamesRoom> room = managerFactory.gamesRoomManager().getRoom(roomId);
                if (!room.isPresent()) {
                    return false;
                }
                return room.get().joinTable(userId, tableId, name, playerType, skill, deckList, password);

            }
        });
    }

    @Override
    public boolean joinTournamentTable(final String sessionId, final UUID roomId, final UUID tableId, final String name, final PlayerType playerType, final int skill, final DeckCardLists deckList, final String password) throws MageException {
        return false;
    }

    @Override
    public boolean submitDeck(final String sessionId, final UUID tableId, final DeckCardLists deckList) throws MageException {
        return executeWithResult("submitDeck", sessionId, new ActionWithBooleanResult() {
            @Override
            public Boolean execute() throws MageException {
                Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
                if (!session.isPresent()) {
                    return false;
                } else {
                    UUID userId = session.get().getUserId();
                    boolean ret = managerFactory.tableManager().submitDeck(userId, tableId, deckList);
                    return ret;
                }
            }
        });
    }

    @Override
    public void updateDeck(final String sessionId, final UUID tableId, final DeckCardLists deckList) throws MageException {
        execute("updateDeck", sessionId, () -> {
            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {

            } else {
                UUID userId = session.get().getUserId();
                managerFactory.tableManager().updateDeck(userId, tableId, deckList);
            }
        });
    }

    @Override
    //FIXME: why no sessionId here???
    public List<TableView> getTables(UUID roomId) throws MageException {
        try {
            Optional<GamesRoom> room = managerFactory.gamesRoomManager().getRoom(roomId);
            if (room.isPresent()) {
                return room.get().getTables();
            } else {
                return new ArrayList<>();
            }
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    @Override
    //FIXME: why no sessionId here???
    public List<MatchView> getFinishedMatches(UUID roomId) throws MageException {
        try {
            return managerFactory.gamesRoomManager().getRoom(roomId).map(GamesRoom::getFinished).orElse(new ArrayList<>());
        } catch (Exception ex) {
            handleException(ex);
        }
        return new ArrayList<>();
    }

    @Override
    public List<RoomUsersView> getRoomUsers(UUID roomId) throws MageException {
        try {
            Optional<GamesRoom> room = managerFactory.gamesRoomManager().getRoom(roomId);
            if (room.isPresent()) {
                return room.get().getRoomUsersInfo();
            } else {
                return new ArrayList<>();
            }
        } catch (Exception ex) {
            handleException(ex);
        }
        return new ArrayList<>();
    }

    @Override
    //FIXME: why no sessionId here???
    public TableView getTable(UUID roomId, UUID tableId) throws MageException {
        try {
            Optional<GamesRoom> room = managerFactory.gamesRoomManager().getRoom(roomId);
            return room.flatMap(r -> r.getTable(tableId)).orElse(null);

        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    @Override
    public boolean ping(String sessionId, String pingInfo) {
        return managerFactory.sessionManager().extendUserSession(sessionId, pingInfo);
    }

    @Override
    public boolean startMatch(final String sessionId, final UUID roomId, final UUID tableId) throws MageException {
        Optional<TableController> controller = managerFactory.tableManager().getController(tableId);
        if (!controller.isPresent()) {
            return false;
        }
        if (!controller.get().changeTableStateToStarting()) {
            return false;
        }
        execute("startMatch", sessionId, () -> {
            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
            } else {
                UUID userId = session.get().getUserId();
                managerFactory.tableManager().startMatch(userId, roomId, tableId);
            }
        });
        return true;
    }

    @Override
    public boolean startTournament(final String sessionId, final UUID roomId, final UUID tableId) throws MageException {
        Optional<TableController> controller = managerFactory.tableManager().getController(tableId);
        if (!controller.isPresent()) {
            return false;
        }
        if (!controller.get().changeTableStateToStarting()) {
            return false;
        }
        execute("startTournament", sessionId, () -> {
            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
            } else {
                UUID userId = session.get().getUserId();
                managerFactory.tableManager().startTournament(userId, roomId, tableId);
            }
        });
        return true;
    }

    @Override
    //FIXME: why no sessionId here???
    public TournamentView getTournament(UUID tournamentId) throws MageException {
        return null;
    }

    @Override
    //FIXME: why no sessionId here???
    public void sendChatMessage(final UUID chatId, final String userName, final String message) throws MageException {
    }

    @Override
    public void joinChat(final UUID chatId, final String sessionId, final String userName) throws MageException {
        execute("joinChat", sessionId, () -> {
            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {

                UUID userId = session.getUserId();
                managerFactory.chatManager().joinChat(chatId, userId);
            });
        });
    }

    @Override
    public void leaveChat(final UUID chatId, final String sessionId) throws MageException {
        execute("leaveChat", sessionId, () -> {
            if (chatId != null) {
                managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                    UUID userId = session.getUserId();
                    managerFactory.chatManager().leaveChat(chatId, userId);
                });
            }
        });
    }

    @Override
    //FIXME: why no sessionId here???
    public UUID getMainRoomId() throws MageException {
        try {
            return managerFactory.gamesRoomManager().getMainRoomId();
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    @Override
    //FIXME: why no sessionId here???
    public UUID getRoomChatId(UUID roomId) throws MageException {
        try {
            Optional<GamesRoom> room = managerFactory.gamesRoomManager().getRoom(roomId);
            if (!room.isPresent()) {
                return null;
            }
            return room.get().getChatId();
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    @Override
    public boolean isTableOwner(final String sessionId, UUID roomId, final UUID tableId) throws MageException {
        return executeWithResult("isTableOwner", sessionId, new ActionWithBooleanResult() {
            @Override
            public Boolean execute() {
                Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
                if (!session.isPresent()) {
                    return false;
                } else {
                    UUID userId = session.get().getUserId();
                    return managerFactory.tableManager().isTableOwner(tableId, userId);
                }
            }
        });
    }

    @Override
    public void swapSeats(final String sessionId, final UUID roomId, final UUID tableId, final int seatNum1, final int seatNum2) throws MageException {
        execute("swapSeats", sessionId, () -> {
            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                managerFactory.tableManager().swapSeats(tableId, userId, seatNum1, seatNum2);
            });
        });
    }

    @Override
    public boolean leaveTable(final String sessionId, final UUID roomId, final UUID tableId) throws MageException {
        Optional<TableController> tableController = managerFactory.tableManager().getController(tableId);
        if (tableController.isPresent()) {
            TableState tableState = tableController.get().getTableState();
            if (tableState != TableState.WAITING && tableState != TableState.READY_TO_START) {
                // table was already started, so player can't leave anymore now
                return false;
            }
            execute("leaveTable", sessionId, () -> {
                managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                    UUID userId = session.getUserId();
                    managerFactory.gamesRoomManager().getRoom(roomId).ifPresent(room ->
                            room.leaveTable(userId, tableId));

                });
            });
        } else {
            // this can happen if a game ends and a player quits XMage or a match nearly at the same time as the game ends
        }
        return true;
    }

    @Override
    //FIXME: why no sessionId here???
    public UUID getTableChatId(UUID tableId) throws MageException {
        try {
            return managerFactory.tableManager().getChatId(tableId).orElse(null);
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    @Override
    public void joinGame(final UUID gameId, final String sessionId) throws MageException {
        execute("joinGame", sessionId, () -> {
            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                managerFactory.gameManager().joinGame(gameId, userId);
            });
        });
    }

    @Override
    public void joinDraft(final UUID draftId, final String sessionId) throws MageException {
        execute("joinDraft", sessionId, () -> {
            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                managerFactory.draftManager().joinDraft(draftId, userId);
            });
        });
    }

    @Override
    public void joinTournament(final UUID tournamentId, final String sessionId) throws MageException {
        execute("joinTournament", sessionId, () -> {
            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
            } else {
                UUID userId = session.get().getUserId();
                managerFactory.tournamentManager().joinTournament(tournamentId, userId);
            }
        });
    }

    @Override
    //FIXME: why no sessionId here???
    public UUID getGameChatId(UUID gameId) throws MageException {
        try {
            return managerFactory.gameManager().getChatId(gameId).orElse(null);
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    @Override
    //FIXME: why no sessionId here???
    public UUID getTournamentChatId(UUID tournamentId) throws MageException {
        try {
            return managerFactory.tournamentManager().getChatId(tournamentId).orElse(null);
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    @Override
    public void sendPlayerUUID(final UUID gameId, final String sessionId, final UUID data) throws MageException {
        execute("sendPlayerUUID", sessionId, () -> {
            Optional<User> user = managerFactory.sessionManager().getUser(sessionId);
            if (user.isPresent()) {
                user.get().sendPlayerUUID(gameId, data);
            } else {
            }
        });
    }

    @Override
    public void sendPlayerString(final UUID gameId, final String sessionId, final String data) throws MageException {
        execute("sendPlayerString", sessionId, () -> {
            Optional<User> user = managerFactory.sessionManager().getUser(sessionId);
            if (user.isPresent()) {
                user.get().sendPlayerString(gameId, data);
            } else {
            }
        });
    }

    @Override
    public void sendPlayerManaType(final UUID gameId, final UUID playerId, final String sessionId, final ManaType data) throws MageException {
        execute("sendPlayerManaType", sessionId, () -> {
            Optional<User> user = managerFactory.sessionManager().getUser(sessionId);
            if (user.isPresent()) {
                user.get().sendPlayerManaType(gameId, playerId, data);
            } else {
            }
        });
    }

    @Override
    public void sendPlayerBoolean(final UUID gameId, final String sessionId, final Boolean data) throws MageException {
        execute("sendPlayerBoolean", sessionId, () -> {
            Optional<User> user = managerFactory.sessionManager().getUser(sessionId);
            if (user.isPresent()) {
                user.get().sendPlayerBoolean(gameId, data);
            } else {
            }
        });
    }

    @Override
    public void sendPlayerInteger(final UUID gameId, final String sessionId, final Integer data) throws MageException {
        execute("sendPlayerInteger", sessionId, () -> {
            Optional<User> user = managerFactory.sessionManager().getUser(sessionId);
            if (user.isPresent()) {
                user.get().sendPlayerInteger(gameId, data);
            } else {
            }
        });
    }

    @Override
    public DraftPickView sendCardPick(final UUID draftId, final String sessionId, final UUID cardPick, final Set<UUID> hiddenCards) throws MageException {
        return executeWithResult("sendCardPick", sessionId, new DraftPickViewActionWithNullNegativeResult(sessionId, draftId, cardPick, hiddenCards));
    }

    @Override
    public void sendCardMark(final UUID draftId, final String sessionId, final UUID cardPick) throws MageException {
        execute("sendCardMark", sessionId, () -> {
            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                managerFactory.draftManager().sendCardMark(draftId, userId, cardPick);
            });
        });
    }

    @Override
    public void quitMatch(final UUID gameId, final String sessionId) throws MageException {
        execute("quitMatch", sessionId, () -> {
            try {
                callExecutor.execute(
                        () -> {
                            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                                UUID userId = session.getUserId();
                                managerFactory.gameManager().quitMatch(gameId, userId);
                            });
                        }
                );
            } catch (Exception ex) {
                handleException(ex);
            }
        });
    }

    @Override
    public void quitTournament(final UUID tournamentId, final String sessionId) throws MageException {
        execute("quitTournament", sessionId, () -> {
            try {
                callExecutor.execute(
                        () -> {
                            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                                UUID userId = session.getUserId();

                                managerFactory.tournamentManager().quit(tournamentId, userId);
                            });
                        }
                );
            } catch (Exception ex) {
                handleException(ex);
            }
        });
    }

    @Override

    public void quitDraft(final UUID draftId, final String sessionId) throws MageException {
    }

    @Override
    public void sendPlayerAction(final PlayerAction playerAction, final UUID gameId, final String sessionId, final Object data) throws MageException {
        execute("sendPlayerAction", sessionId, () -> {
            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                managerFactory.gameManager().sendPlayerAction(playerAction, gameId, userId, data);
            });
        });
    }

    @Override
    public boolean watchTable(final String sessionId, final UUID roomId, final UUID tableId) throws MageException {
        return executeWithResult("setUserData", sessionId, new ActionWithBooleanResult() {
            @Override
            public Boolean execute() throws MageException {
                Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
                if (!session.isPresent()) {
                    return false;
                } else {
                    UUID userId = session.get().getUserId();
                    if (managerFactory.gamesRoomManager().getRoom(roomId).isPresent()) {
                        return managerFactory.gamesRoomManager().getRoom(roomId).get().watchTable(userId, tableId);
                    } else {
                        return false;
                    }
                }
            }
        });
    }

    @Override
    public boolean watchTournamentTable(final String sessionId, final UUID tableId) throws MageException {
        return executeWithResult("setUserData", sessionId, new MyActionWithBooleanResult(sessionId, tableId));
    }

    @Override
    public boolean watchGame(final UUID gameId, final String sessionId) throws MageException {
        return executeWithResult("watchGame", sessionId, new ActionWithResult<Boolean>() {
            @Override
            public Boolean execute() throws MageException {
                return managerFactory.sessionManager().getSession(sessionId)
                        .map(session -> {
                            UUID userId = session.getUserId();
                            return managerFactory.gameManager().watchGame(gameId, userId);
                        }).orElse(false);
            }

            @Override
            public Boolean negativeResult() {
                return false;
            }
        });
    }

    @Override
    public void stopWatching(final UUID gameId, final String sessionId) throws MageException {
        execute("stopWatching", sessionId, () -> {
            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                managerFactory.userManager().getUser(userId).ifPresent(user -> {
                    managerFactory.gameManager().stopWatching(gameId, userId);
                    user.removeGameWatchInfo(gameId);
                });
            });

        });
    }

    @Override
    public void replayGame(final UUID gameId, final String sessionId) throws MageException {
        execute("replayGame", sessionId, () -> {
            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                managerFactory.replayManager().replayGame(gameId, userId);
            });
        });
    }

    @Override
    public void startReplay(final UUID gameId, final String sessionId) throws MageException {
        execute("startReplay", sessionId, () -> {
            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                managerFactory.replayManager().startReplay(gameId, userId);
            });
        });
    }

    @Override
    public void stopReplay(final UUID gameId, final String sessionId) throws MageException {
        execute("stopReplay", sessionId, () -> {
            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
            } else {
                UUID userId = session.get().getUserId();
                managerFactory.replayManager().stopReplay(gameId, userId);
            }
        });
    }

    @Override
    public void nextPlay(final UUID gameId, final String sessionId) throws MageException {
        execute("nextPlay", sessionId, () -> {
            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
            } else {
                UUID userId = session.get().getUserId();
                managerFactory.replayManager().nextPlay(gameId, userId);
            }
        });
    }

    @Override
    public void previousPlay(final UUID gameId, final String sessionId) throws MageException {
        execute("previousPlay", sessionId, () -> {
            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
            } else {
                UUID userId = session.get().getUserId();
                managerFactory.replayManager().previousPlay(gameId, userId);
            }
        });
    }

    @Override
    public void skipForward(final UUID gameId, final String sessionId, final int moves) throws MageException {
        execute("skipForward", sessionId, () -> {
            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
            } else {
                UUID userId = session.get().getUserId();
                managerFactory.replayManager().skipForward(gameId, userId, moves);
            }
        });
    }

    @Override
    //TODO: check how often it is used
    public ServerState getServerState() throws MageException {
        try {
            return new ServerState(
                    GameFactory.instance.getGameTypes(),
                    PlayerFactory.instance.getPlayerTypes().toArray(new PlayerType[PlayerFactory.instance.getPlayerTypes().size()]),
                    DeckValidatorFactory.instance.getDeckTypes().toArray(new String[DeckValidatorFactory.instance.getDeckTypes().size()]),
                    CubeFactory.instance.getDraftCubes().toArray(new String[CubeFactory.instance.getDraftCubes().size()]),
                    testMode,
                    Main.getVersion(),
                    CardRepository.instance.getContentVersionConstant(),
                    ExpansionRepository.instance.getContentVersionConstant()
            );
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    @Override
    public void cheat(final UUID gameId, final String sessionId, final UUID playerId, final DeckCardLists deckList) throws MageException {
        execute("cheat", sessionId, () -> {
            if (testMode) {
                managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                    UUID userId = session.getUserId();
                    managerFactory.gameManager().cheat(gameId, userId, playerId, deckList);
                });
            }
        });
    }

    @Override
    public boolean cheat(final UUID gameId, final String sessionId, final UUID playerId, final String cardName) throws MageException {
        return executeWithResult("cheatOne", sessionId, new ActionWithBooleanResult() {
            @Override
            public Boolean execute() {
                if (testMode) {
                    Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
                    if (!session.isPresent()) {
                    } else {
                        UUID userId = session.get().getUserId();
                        return managerFactory.gameManager().cheat(gameId, userId, playerId, cardName);
                    }
                }
                return false;
            }
        });
    }

    public void handleException(Exception ex) throws MageException {
        if (!ex.getMessage().equals("No message")) {
            throw new MageException("Server error: " + ex.getMessage());
        }
    }

    @Override
    public GameView getGameView(final UUID gameId, final String sessionId, final UUID playerId) throws MageException {
        return executeWithResult("getGameView", sessionId, new GameViewActionWithNullNegativeResult(sessionId, gameId, playerId));
    }

    /**
     * Get user data for admin console
     *
     * @param sessionId
     * @return
     * @throws MageException
     */
    @Override
    public List<UserView> getUsers(String sessionId) throws MageException {
        return executeWithResult("getUsers", sessionId, new ListActionWithNullNegativeResult(), true);
    }

    @Override
    public void disconnectUser(final String sessionId, final String userSessionId) throws MageException {
        execute("disconnectUser", sessionId, () -> managerFactory.sessionManager().disconnectUser(sessionId, userSessionId));
    }

    @Override
    public void muteUser(final String sessionId, final String userName, final long durationMinutes) throws MageException {
        execute("muteUser", sessionId, () -> {
            managerFactory.userManager().getUserByName(userName).ifPresent(user -> {
                Date muteUntil = new Date(Calendar.getInstance().getTimeInMillis() + (durationMinutes * Timer.ONE_MINUTE));
                user.showUserMessage("Admin info", "You were muted for chat messages until " + SystemUtil.dateFormat.format(muteUntil) + '.');
                user.setChatLockedUntil(muteUntil);
            });

        });
    }

    @Override
    public void lockUser(final String sessionId, final String userName, final long durationMinutes) throws MageException {
        execute("lockUser", sessionId, () -> {
            managerFactory.userManager().getUserByName(userName).ifPresent(user -> {
                Date lockUntil = new Date(Calendar.getInstance().getTimeInMillis() + (durationMinutes * Timer.ONE_MINUTE));
                user.showUserMessage("Admin info", "Your user profile was locked until " + SystemUtil.dateFormat.format(lockUntil) + '.');
                user.setLockedUntil(lockUntil);
                if (user.isConnected()) {
                    managerFactory.sessionManager().disconnectUser(sessionId, user.getSessionId());
                }
            });

        });
    }

    @Override
    public void setActivation(final String sessionId, final String userName, boolean active) throws MageException {
        execute("setActivation", sessionId, () -> {
            AuthorizedUser authorizedUser = AuthorizedUserRepository.getInstance().getByName(userName);
            Optional<User> u = managerFactory.userManager().getUserByName(userName);
            if (u.isPresent()) {
                User user = u.get();
                user.setActive(active);
                if (!user.isActive() && user.isConnected()) {
                    managerFactory.sessionManager().disconnectUser(sessionId, user.getSessionId());
                }
            } else if (authorizedUser != null) {
                User theUser = new User(managerFactory, userName, "localhost", authorizedUser);
                theUser.setActive(active);
            }

        });
    }

    @Override
    public void toggleActivation(final String sessionId, final String userName) throws MageException {
        execute("toggleActivation", sessionId, ()
                -> managerFactory.userManager().getUserByName(userName).ifPresent(user
                -> {
            user.setActive(!user.isActive());
            if (!user.isActive() && user.isConnected()) {
                managerFactory.sessionManager().disconnectUser(sessionId, user.getSessionId());
            }
        }));
    }

    @Override
    public void endUserSession(final String sessionId, final String userSessionId) throws MageException {
        execute("endUserSession", sessionId, () -> managerFactory.sessionManager().endUserSession(sessionId, userSessionId));
    }

    /**
     * Admin console - Remove table
     *
     * @param sessionId
     * @param tableId
     * @throws MageException
     */
    @Override
    public void removeTable(final String sessionId, final UUID tableId) throws MageException {
        execute("removeTable", sessionId, () -> {
            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                managerFactory.tableManager().removeTable(userId, tableId);
            });
        });
    }

    @Override
    public Object getServerMessagesCompressed(String sessionId) throws MageException {
        return executeWithResult("getGameView", sessionId, new MyActionWithNullNegativeResult());
    }

    @Override
    public void sendFeedbackMessage(final String sessionId, final String username, final String title, final String type, final String message, final String email) throws MageException {
        if (title != null && message != null) {
            execute("sendFeedbackMessage", sessionId, ()
                    -> managerFactory.sessionManager().getSession(sessionId).ifPresent(
                    session -> FeedbackServiceImpl.instance.feedback(username, title, type, message, email, session.getHost())
            ));
        }
    }

    @Override
    public void sendBroadcastMessage(final String sessionId, final String message) throws MageException {
    }

    private void sendErrorMessageToClient(final String sessionId, final String message) throws MageException {
        execute("sendErrorMessageToClient", sessionId, () -> managerFactory.sessionManager().sendErrorMessageToClient(sessionId, message));
    }

    protected void execute(final String actionName, final String sessionId, final Action action, boolean checkAdminRights) throws MageException {
        if (checkAdminRights) {
            if (!managerFactory.sessionManager().isAdmin(sessionId)) {
                return;
            }
        }
        execute(actionName, sessionId, action);
    }

    protected void execute(final String actionName, final String sessionId, final Action action) throws MageException {
        if (managerFactory.sessionManager().isValidSession(sessionId)) {
            try {
                callExecutor.execute(
                        () -> {
                            if (managerFactory.sessionManager().isValidSession(sessionId)) {
                                try {
                                    action.execute();
                                } catch (MageException me) {
                                    throw new RuntimeException(me);
                                }
                            }
                        }
                );
            } catch (Exception ex) {
                handleException(ex);
            }
        }
    }

    protected <T> T executeWithResult(String actionName, final String sessionId, final ActionWithResult<T> action, boolean checkAdminRights) throws MageException {
        if (checkAdminRights) {
            if (!managerFactory.sessionManager().isAdmin(sessionId)) {
                return action.negativeResult();
            }
        }
        return executeWithResult(actionName, sessionId, action);
    }

    //TODO: also run in threads with future task
    protected <T> T executeWithResult(String actionName, final String sessionId, final ActionWithResult<T> action) throws MageException {
        if (managerFactory.sessionManager().isValidSession(sessionId)) {
            try {
                return action.execute();
            } catch (Exception ex) {
                handleException(ex);
            }
        }
        return action.negativeResult();
    }

    @Override
    public List<ExpansionInfo> getMissingExpansionData(List<String> codes) {
        List<ExpansionInfo> result = new ArrayList<>();
        for (ExpansionInfo expansionInfo : ExpansionRepository.instance.getAll()) {
            if (!codes.contains(expansionInfo.getCode())) {
                result.add(expansionInfo);
            }
        }
        return result;
    }

    @Override
    public List<CardInfo> getMissingCardsData(List<String> classNames) {
        return null;
    }

    private static class MyActionWithNullNegativeResult extends ActionWithNullNegativeResult<Object> {

        @Override
        public Object execute() throws MageException {
            return CompressUtil.compress(ServerMessagesUtil.instance.getMessages());
        }
    }

    private class ListActionWithNullNegativeResult extends ActionWithNullNegativeResult<List<UserView>> {

        @Override
        public List<UserView> execute() throws MageException {
            return null;
        }
    }

    private class GameViewActionWithNullNegativeResult extends ActionWithNullNegativeResult<GameView> {

        private final String sessionId;
        private final UUID gameId;
        private final UUID playerId;

        public GameViewActionWithNullNegativeResult(String sessionId, UUID gameId, UUID playerId) {
            this.sessionId = sessionId;
            this.gameId = gameId;
            this.playerId = playerId;
        }

        @Override
        public GameView execute() throws MageException {
            return null;
        }
    }

    private class MyActionWithBooleanResult extends ActionWithBooleanResult {

        private final String sessionId;
        private final UUID tableId;

        public MyActionWithBooleanResult(String sessionId, UUID tableId) {
            this.sessionId = sessionId;
            this.tableId = tableId;
        }

        @Override
        public Boolean execute() throws MageException {
            return false;
        }
    }

    private class DraftPickViewActionWithNullNegativeResult extends ActionWithNullNegativeResult<DraftPickView> {

        private final String sessionId;
        private final UUID draftId;
        private final UUID cardPick;
        private final Set<UUID> hiddenCards;

        public DraftPickViewActionWithNullNegativeResult(String sessionId, UUID draftId, UUID cardPick, Set<UUID> hiddenCards) {
            this.sessionId = sessionId;
            this.draftId = draftId;
            this.cardPick = cardPick;
            this.hiddenCards = hiddenCards;
        }

        @Override
        public DraftPickView execute() {
            return null;
        }
    }

    private class MyActionWithTableViewResult extends ActionWithTableViewResult {

        private final String sessionId;
        private final MatchOptions options;
        private final UUID roomId;

        public MyActionWithTableViewResult(String sessionId, MatchOptions options, UUID roomId) {
            this.sessionId = sessionId;
            this.options = options;
            this.roomId = roomId;
        }

        @Override
        public TableView execute() throws MageException {
            return null;
        }

    }
}
