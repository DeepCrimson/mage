package mage.server.game;

import mage.game.match.Match;
import mage.game.match.MatchOptions;
import mage.game.match.MatchType;
import mage.view.GameTypeView;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author BetaSteward_at_googlemail.com
 */
public enum GameFactory {

    instance;

    private final Map<String, Class<Match>> games = new HashMap<>();
    private final Map<String, MatchType> gameTypes = new HashMap<>();
    private final List<GameTypeView> gameTypeViews = new ArrayList<>();


    GameFactory() {
    }

    public Match createMatch(String gameType, MatchOptions options) {

        Match match;
        try {
            Constructor<Match> con = games.get(gameType).getConstructor(MatchOptions.class);
            match = con.newInstance(options);
        } catch (Exception ex) {
            return null;
        }

        return match;
    }

    public List<GameTypeView> getGameTypes() {
        return gameTypeViews;
    }

    public void addGameType(String name, MatchType matchType, Class game) {
        if (game != null) {
            this.games.put(name, game);
            this.gameTypes.put(name, matchType);
            this.gameTypeViews.add(new GameTypeView(matchType));
        }
    }

}
