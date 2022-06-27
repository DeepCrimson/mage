package org.mage.test.cards.single.snc;

import mage.abilities.Ability;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.game.permanent.Permanent;
import org.junit.Before;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.UUID;

/**
 * {@link mage.cards.u.UnlicensedHearse Unlicensed Hearse}
 * {2}
 * Artifact - Vehicle
 * {T}: Exile up to two target cards from a single graveyard.
 * Unlicensed Hearse's power and toughness are each equal to the
 * number of cards exiled with it.
 * Crew 2
 *
 * @author DeepCrimson
 */
public class UnlicensedHearseTest extends CardTestPlayerBase {

    private UUID unlicensedHearseID;

    /**
     * Give Player A one Unlicensed Hearse on the battlefield and give
     * Player B two cards in their graveyard.
     */
    @Before
    public void createHearseAndFillGraveyard() {
        unlicensedHearseID = addCard(Zone.BATTLEFIELD, playerA, "Unlicensed Hearse").get(0);
        addCard(Zone.GRAVEYARD, playerB, "Grizzly Bears");
        addCard(Zone.GRAVEYARD, playerB, "Forest Bear");
    }

    /**
     * Test that exiling a single card with Unlicensed Hearse results
     * in it being a 1/1.
     */
    @Test
    public void testExileOneCardFromGraveyard() {
        Permanent unlicensedHearse = currentGame.getPermanent(unlicensedHearseID);
        Ability ability = unlicensedHearse.getAbilities().get(0);
        String abilityName = ability.getRule();
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA,
                abilityName,
                "Grizzly Bears");

        setStrictChooseMode(true);
        execute();
        assertAllCommandsUsed();

        assertGraveyardCount(playerB, 1);
        assertPowerToughness(unlicensedHearseID, 1, 1);
    }

    /**
     * Test that exiling two cards with Unlicensed Hearse from one
     * graveyard results in it being a 2/2.
     */
    @Test
    public void testExileTwoCardsFromGraveyard() {
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA,
                "{T}: Exile up to two target cards from a single graveyard.",
                new String[]{"Grizzly Bears", "Forest Bear"});

        setStrictChooseMode(true);
        execute();
        assertAllCommandsUsed();

        assertGraveyardCount(playerB, 0);
        assertPowerToughness(unlicensedHearseID, 2, 2);
    }
}
