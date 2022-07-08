package mage.util.trace;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.StaticAbility;
import mage.abilities.TriggeredAbility;
import mage.abilities.effects.ContinuousEffectsList;
import mage.abilities.effects.RestrictionEffect;
import mage.abilities.keyword.CantBeBlockedSourceAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.IntimidateAbility;
import mage.abilities.keyword.ReachAbility;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.combat.Combat;
import mage.game.combat.CombatGroup;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

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
        // trace non-flying vs flying
        for (CombatGroup group : combat.getGroups()) {
            for (UUID attackerId : group.getAttackers()) {
                Permanent attacker = game.getPermanent(attackerId);
                if (attacker != null) {
                    if (hasFlying(attacker)) {
                        // traceCombat(game, attacker, null);
                        for (UUID blockerId : group.getBlockers()) {
                            Permanent blocker = game.getPermanent(blockerId);
                            if (blocker != null && !hasFlying(blocker) && !hasReach(blocker)) {
                                traceCombat(game, attacker, blocker);
                            }
                        }
                    }
                    if (hasIntimidate(attacker)) {
                        for (UUID blockerId : group.getBlockers()) {
                            Permanent blocker = game.getPermanent(blockerId);
                            if (blocker != null && !blocker.isArtifact(game)
                                    && !attacker.getColor(game).shares(blocker.getColor(game))) {
                                traceCombat(game, attacker, blocker);
                            }
                        }
                    }
                    if (cantBeBlocked(attacker)) {
                        if (!group.getBlockers().isEmpty()) {
                            Permanent blocker = game.getPermanent(group.getBlockers().get(0));
                            if (blocker != null) {
                                traceCombat(game, attacker, blocker);
                            }
                        }
                    }
                }
            }
        }
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

    private static void traceCombat(Game game, Permanent attacker, Permanent blocker) {
        String prefix = "> ";
        if (blocker != null) {
        }

        for (Ability ability : attacker.getAbilities()) {
        }
        if (blocker != null) {
            for (Ability ability : blocker.getAbilities()) {
            }
        }


        Map<RestrictionEffect, Set<Ability>> attackerResEffects = game.getContinuousEffects().getApplicableRestrictionEffects(attacker, game);
        for (Map.Entry<RestrictionEffect, Set<Ability>> entry : attackerResEffects.entrySet()) {
            for (Ability ability : entry.getValue()) {
            }
        }
        if (blocker != null) {
            Map<RestrictionEffect, Set<Ability>> blockerResEffects = game.getContinuousEffects().getApplicableRestrictionEffects(blocker, game);
            for (Map.Entry<RestrictionEffect, Set<Ability>> entry : blockerResEffects.entrySet()) {
                for (Ability ability : entry.getValue()) {
                }
            }
        }
        ContinuousEffectsList<RestrictionEffect> restrictionEffects = (ContinuousEffectsList<RestrictionEffect>) game.getContinuousEffects().getRestrictionEffects();
        for (RestrictionEffect effect : restrictionEffects) {
        }

        traceForPermanent(game, attacker, prefix, restrictionEffects);
        if (blocker != null) {
            traceForPermanent(game, blocker, prefix, restrictionEffects);
        }

    }

    private static void traceForPermanent(Game game, Permanent permanent, String uuid, ContinuousEffectsList<RestrictionEffect> restrictionEffects) {
        for (RestrictionEffect effect : restrictionEffects) {
            for (Ability ability : restrictionEffects.getAbility(effect.getId())) {
                if (!(ability instanceof StaticAbility) || ability.isInUseableZone(game, permanent, null)) {
                } else {
                    boolean usable = ability.isInUseableZone(game, permanent, null);
                    if (!usable) {
                        Zone zone = ability.getZone();
                        MageObject object = game.getObject(ability.getSourceId());
                        if (object != null) {
                        }
                        Zone test = game.getState().getZone(ability.getSourceId());
                    }
                }
            }
        }
    }

    public static void trace(String msg) {
    }

    /**
     * Prints out a status of the currently existing triggered abilities
     *
     * @param game
     */
    public static void traceTriggeredAbilities(Game game) {
        Map<String, String> orderedAbilities = new TreeMap<>();
        for (Map.Entry<String, TriggeredAbility> entry : game.getState().getTriggers().entrySet()) {
            Player controller = game.getPlayer(entry.getValue().getControllerId());
            MageObject source = game.getObject(entry.getValue().getSourceId());
            orderedAbilities.put((controller == null ? "no controller" : controller.getName()) + (source == null ? "no source" : source.getIdName()) + entry.getKey(), entry.getKey());
        }
        String playerName = "";
        for (Map.Entry<String, String> entry : orderedAbilities.entrySet()) {
            TriggeredAbility trAbility = game.getState().getTriggers().get(entry.getValue());
            Player controller = game.getPlayer(trAbility.getControllerId());
            MageObject source = game.getObject(trAbility.getSourceId());
            if (!controller.getName().equals(playerName)) {
                playerName = controller.getName();
            }
        }
    }
}
