package mage.actions.score;

import mage.cards.Card;
import mage.game.Game;

/**
 * @author ayratn
 */
public class ArtificialScoringSystem implements ScoringSystem {

    public static ArtificialScoringSystem inst;

    static {
        inst = new ArtificialScoringSystem();
    }

    /**
     * Lose score is lowered in function of the turn and phase when it occurs.
     * Encourages AI to win as fast as possible.
     *
     * @param game
     * @return
     */
    @Override
    public int getLoseGameScore(final Game game) {
        if (game.getStep() == null) {
            return 0;
        }
        return ScoringConstants.LOSE_GAME_SCORE + game.getTurnNum() * 2500 + game.getStep().getType().getIndex() * 200;
    }

    @Override
    public int getCardScore(Card card) {
        //TODO: implement
        return ScoringConstants.UNKNOWN_CARD_SCORE;
    }

}
