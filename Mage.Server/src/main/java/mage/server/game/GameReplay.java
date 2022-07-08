package mage.server.game;

import mage.game.Game;
import mage.game.GameState;
import mage.game.GameStates;
import mage.server.Main;
import mage.util.CopierObjectInputStream;
import mage.utils.StreamUtils;

import java.io.*;
import java.util.UUID;
import java.util.zip.GZIPInputStream;


/**
 * @author BetaSteward_at_googlemail.com
 */
public class GameReplay {

    private final GameStates savedGame;
    private final Game game;
    private int stateIndex;

    public GameReplay(UUID gameId) {
        this.game = loadGame(gameId);
        this.savedGame = game.getGameStates();
    }

    public void start() {
        this.stateIndex = 0;
    }

    public GameState next() {
        if (this.stateIndex < savedGame.getSize()) {
            return savedGame.get(stateIndex++);
        }
        return null;
    }

    public GameState previous() {
        if (this.stateIndex > 0) {
            return savedGame.get(--stateIndex);
        }
        return null;
    }

    public Game getGame() {
        return this.game;
    }

    private Game loadGame(UUID gameId) {
        InputStream file = null;
        InputStream buffer = null;
        InputStream gzip = null;
        ObjectInput input = null;
        try {
            file = new FileInputStream("saved/" + gameId.toString() + ".game");
            buffer = new BufferedInputStream(file);
            gzip = new GZIPInputStream(buffer);
            input = new CopierObjectInputStream(Main.classLoader, gzip);
            Game loadGame = (Game) input.readObject();
            GameStates states = (GameStates) input.readObject();
            loadGame.loadGameStates(states);
            return loadGame;

        } catch (ClassNotFoundException ex) {
        } catch (IOException ex) {
        } finally {
            StreamUtils.closeQuietly(file);
            StreamUtils.closeQuietly(buffer);
            StreamUtils.closeQuietly(input);
            StreamUtils.closeQuietly(gzip);
        }
        return null;
    }

}
