package org.mage.web.resolver;

import graphql.kickstart.tools.GraphQLQueryResolver;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.basiclands.Forest;
import mage.cards.g.GrizzlyBears;
import mage.constants.Rarity;
import org.springframework.stereotype.Component;

@Component
public class CardResolver implements GraphQLQueryResolver {

    public Card card(String id) {
        Card card = null;
        if (id.equals("1")) {
            card = CardImpl.createCard(
                    GrizzlyBears.class,
                    new CardSetInfo(
                            "Grizzly Bears",
                            "10E",
                            "268",
                            Rarity.COMMON)
            );
        } else if (id.equals("0")) {
            card = CardImpl.createCard(
                    Forest.class,
                    new CardSetInfo(
                            "Forest",
                            "10E",
                            "380",
                            Rarity.COMMON)
            );
        }
        return card;
    }
}
