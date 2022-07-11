

package mage.view;

import java.io.Serializable;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class DraftPickView implements Serializable {
    private static final long serialVersionUID = 1L;

    protected SimpleCardsView booster;
    protected SimpleCardsView picks;
    protected boolean picking;
    protected int timeout;

    public SimpleCardsView getBooster() {
        return booster;
    }

    public SimpleCardsView getPicks() {
        return picks;
    }

    public boolean isPicking() {
        return this.picking;
    }

    public int getTimeout() {
        return timeout;
    }
}
