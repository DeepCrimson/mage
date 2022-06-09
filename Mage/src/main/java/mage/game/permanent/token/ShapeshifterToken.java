package mage.game.permanent.token;

import mage.MageInt;
import mage.abilities.keyword.ChangelingAbility;
import mage.constants.CardType;
import mage.constants.SubType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author TheElk801
 */
public final class ShapeshifterToken extends TokenImpl {


    public ShapeshifterToken() {
        super("Shapeshifter Token", "2/2 colorless Shapeshifter creature token with changeling");
        cardType.add(CardType.CREATURE);
        subtype.add(SubType.SHAPESHIFTER);
        power = new MageInt(2);
        toughness = new MageInt(2);
        addAbility(new ChangelingAbility());

        availableImageSetCodes = Arrays.asList("MH1", "CLB");
    }

    public ShapeshifterToken(final ShapeshifterToken token) {
        super(token);
    }

    public ShapeshifterToken copy() {
        return new ShapeshifterToken(this);
    }
}
