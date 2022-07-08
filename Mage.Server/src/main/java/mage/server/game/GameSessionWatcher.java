package mage.server.game;

import mage.game.Game;
import mage.server.managers.UserManager;
import org.apache.log4j.Logger;

import java.util.UUID;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class GameSessionWatcher {

    protected static final Logger logger = Logger.getLogger(GameSessionWatcher.class);

    private final UserManager userManager;
    protected final UUID userId;
    protected final Game game;
    protected boolean killed = false;
    protected final boolean isPlayer;

    public GameSessionWatcher(UserManager userManager, UUID userId, Game game, boolean isPlayer) {
        this.userManager = userManager;
        this.userId = userId;
        this.game = game;
        this.isPlayer = isPlayer;
    }

    public boolean init() {
        return false;
    }

    public void update() {
    }

    public void inform(final String message) {
    }

    public void informPersonal(final String message) {
    }

    public void gameOver(final String message) {
    }

    /**
     * Cleanup if Session ends
     */
    public void cleanUp() {

    }

    public void gameError(final String message) {
    }

    public boolean isPlayer() {
        return isPlayer;
    }

}
