package mage.util.trace;

import mage.abilities.Ability;
import mage.abilities.keyword.CantBeBlockedSourceAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.IntimidateAbility;
import mage.abilities.keyword.ReachAbility;
import mage.game.Game;
import mage.game.combat.Combat;
import mage.game.permanent.Permanent;

/**
 * @author magenoxx_at_gmail.com
 */
public final class TraceUtil {

    /**
     * This method is intended to catch various bugs with combat.
     * <p>
     * One of them (possibly the most annoying) is when creature without flying or reach blocks creature with flying.
     * No test managed to reproduce it, but it happens in the games time to time and was reported by different players.
     * <p>
     * The idea: is to catch such cases manually and print out as much information from game state that may help as possible.
     *
     * @param game
     * @param combat
     */
    public static void traceCombatIfNeeded(Game game, Combat combat) {
    }

    /**
     * We need this to check Flying existence in not-common way: by instanceof.
     *
     * @return
     */
    private static boolean hasFlying(Permanent permanent) {
        for (Ability ability : permanent.getAbilities()) {
            if (ability instanceof FlyingAbility) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasIntimidate(Permanent permanent) {
        for (Ability ability : permanent.getAbilities()) {
            if (ability instanceof IntimidateAbility) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasReach(Permanent permanent) {
        for (Ability ability : permanent.getAbilities()) {
            if (ability instanceof ReachAbility) {
                return true;
            }
        }
        return false;
    }

    private static boolean cantBeBlocked(Permanent permanent) {
        for (Ability ability : permanent.getAbilities()) {
            if (ability instanceof CantBeBlockedSourceAbility) {
                return true;
            }
        }
        return false;
    }

    public static void trace(String msg) {
    }

    /**
     * Prints out a status of the currently existing triggered abilities
     *
     * @param game
     */
    public static void traceTriggeredAbilities(Game game) {
    }
}
