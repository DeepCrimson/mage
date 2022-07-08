package mage.server.game;

import mage.choices.Choice;
import mage.constants.ManaType;
import mage.game.Game;
import mage.game.Table;
import mage.players.Player;
import mage.server.managers.ManagerFactory;
import mage.server.managers.UserManager;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class GameSessionPlayer extends GameSessionWatcher {

    private final UserManager userManager;
    private final UUID playerId;

    private final ExecutorService callExecutor;

    public GameSessionPlayer(ManagerFactory managerFactory, Game game, UUID userId, UUID playerId) {
        super(managerFactory.userManager(), userId, game, true);
        this.userManager = managerFactory.userManager();
        callExecutor = managerFactory.threadExecutor().getCallExecutor();
        this.playerId = playerId;
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
    }

    public void ask(final String question, final Map<String, Serializable> options) {
    }

    public void target(final String question, final Set<UUID> targets, final boolean required, final Map<String, Serializable> options) {
    }

    public void select(final String message, final Map<String, Serializable> options) {
    }

    public void chooseAbility() {
    }

    public void chooseChoice(final Choice choice) {
    }

    public void playMana(final String message, final Map<String, Serializable> options) {
    }

    public void playXMana(final String message) {
    }

    public void getAmount(final String message, final int min, final int max) {
    }

    public void getMultiAmount(final List<String> messages, final int min, final int max, final Map<String, Serializable> options) {
    }

    public void endGameInfo(Table table) {
    }

    public void requestPermissionToRollbackTurn(UUID requestingUserId, int numberTurns) {
    }

    public void requestPermissionToSeeHandCards(UUID watcherId) {
    }

    public void sendPlayerUUID(UUID data) {
        game.getPlayer(playerId).setResponseUUID(data);
    }

    public void sendPlayerString(String data) {
        game.getPlayer(playerId).setResponseString(data);
    }

    public void sendPlayerManaType(ManaType manaType, UUID manaTypePlayerId) {
        game.getPlayer(playerId).setResponseManaType(manaTypePlayerId, manaType);
    }

    public void sendPlayerBoolean(Boolean data) {
        game.getPlayer(playerId).setResponseBoolean(data);
    }

    public void sendPlayerInteger(Integer data) {
        game.getPlayer(playerId).setResponseInteger(data);
    }


    public void removeGame() {
        userManager.getUser(userId).ifPresent(user -> user.removeGame(playerId));

    }

    public UUID getGameId() {
        return game.getId();
    }

    public void quitGame() {
        if (game != null) {
            final Player player = game.getPlayer(playerId);
            if (player != null && player.isInGame()) {
                callExecutor.execute(
                        () -> {
                            try {
                                if (game.getStartTime() == null) {
                                    // gameController is still waiting to start the game
                                    player.leave();
                                } else {
                                    // game was already started
                                    player.quit(game);
                                }

                            } catch (Exception ex) {
                            }
                        }
                );

            }
        }
    }

}
