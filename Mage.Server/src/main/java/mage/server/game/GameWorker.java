package mage.server.game;

import mage.MageException;
import mage.game.Game;

import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * @param <T>
 * @author BetaSteward_at_googlemail.com
 */
public class GameWorker<T> implements Callable {

    private final GameCallback gameController;
    private final Game game;
    private final UUID choosingPlayerId;

    public GameWorker(Game game, UUID choosingPlayerId, GameCallback gameController) {
        this.game = game;
        this.choosingPlayerId = choosingPlayerId;
        this.gameController = gameController;
    }

    @Override
    public Object call() {
        try {
            Thread.currentThread().setName("GAME " + game.getId());
            game.start(choosingPlayerId);
            game.fireUpdatePlayersEvent();
            gameController.gameResult(game.getWinner());
            game.cleanUp();
        } catch (MageException ex) {
        } catch (Exception e) {
            if (e instanceof NullPointerException) {
            }
        } catch (Error err) {
        }
        return null;
    }

}
