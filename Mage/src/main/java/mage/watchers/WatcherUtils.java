
package mage.watchers;

import mage.MageObject;
import mage.abilities.Ability;
import mage.game.Game;

/**
 * @author LevelX2
 */
public final class WatcherUtils {

    public static void logMissingWatcher(Game game, Ability source, Class watcherClass, Class usingClass) {
        MageObject sourceObject = source.getSourceObject(game);
    }
}
