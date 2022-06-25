package org.mage.test.cards.abilities.activated;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Before;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class SacrificeCreatureTest extends CardTestPlayerBase {

    @Before
    public void addBloodBairn() {
        addCard(Zone.BATTLEFIELD, playerA, "Blood Bairn");
    }

    /*
     * When playerA controls Blood Bairn and another creature, playerA can
     * activate Blood Bairn's activated ability.
     */
    @Test
    public void testCanActivateSacrificeAbility() {
        // Add Grizzly Bears to battlefield under playerA's control.
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");

        showAvailableAbilities("activated abilities available", 1, PhaseStep.PRECOMBAT_MAIN, playerA);
        showBattlefield("what's on the battlefield", 1, PhaseStep.PRECOMBAT_MAIN, playerA);

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, 2);
    }
}
