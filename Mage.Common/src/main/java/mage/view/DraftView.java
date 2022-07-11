

package mage.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class DraftView implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<String> sets = new ArrayList<>();
    private final List<String> setCodes = new ArrayList<>();
    private final List<String> players = new ArrayList<>();

    public DraftView() {
    }

    public List<String> getSets() {
        return sets;
    }

    public List<String> getSetCodes() {
        return setCodes;
    }

    public List<String> getPlayers() {
        return players;
    }
}
