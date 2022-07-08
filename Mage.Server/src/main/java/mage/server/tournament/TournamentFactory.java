package mage.server.tournament;

import mage.view.TournamentTypeView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author BetaSteward_at_googlemail.com
 */
public enum TournamentFactory {
    instance;
    private final List<TournamentTypeView> tournamentTypeViews = new ArrayList<>();

    public List<TournamentTypeView> getTournamentTypes() {
        return tournamentTypeViews;
    }
}
