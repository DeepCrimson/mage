package mage.watchers;

import mage.game.Game;
import mage.game.events.GameEvent;

import java.util.HashMap;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class Watchers extends HashMap<String, Watcher> {

    public Watchers() {
    }

    private Watchers(final Watchers watchers) {
        watchers.forEach((key, value) -> this.put(key, value.copy()));
    }

    public Watchers copy() {
        return new Watchers(this);
    }

    public void add(Watcher watcher) {
        putIfAbsent(watcher.getKey(), watcher);
    }

    public void watch(GameEvent event, Game game) {
        for (Watcher watcher : this.values()) {
            watcher.watch(event, game);
        }
    }

    public void reset() {
        this.values().forEach(Watcher::reset);
    }

    public Watcher get(String key, String id) {
        return get(id + key);
    }

    @Override
    public Watcher get(Object key) {
        if (containsKey(key)) {
            return super.get(key);
        }
        return null;
    }
}
