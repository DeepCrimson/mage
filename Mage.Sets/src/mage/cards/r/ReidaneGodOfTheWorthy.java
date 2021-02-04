package mage.cards.r;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.PreventionEffectImpl;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.CounterUnlessPaysEffect;
import mage.abilities.effects.common.cost.CostModificationEffectImpl;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.Card;
import mage.cards.CardSetInfo;
import mage.cards.ModalDoubleFacesCard;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.EntersTheBattlefieldEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.stack.StackObject;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class ReidaneGodOfTheWorthy extends ModalDoubleFacesCard {

    public ReidaneGodOfTheWorthy(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new CardType[]{CardType.CREATURE}, new SubType[]{SubType.GOD}, "{2}{W}",
                "Valkmira, Protector's Shield", new CardType[]{CardType.ARTIFACT}, new SubType[]{}, "{3}{W}"
        );

        // 1.
        // Reidane, God of the Worthy
        // Legendary Creature - God
        this.getLeftHalfCard().addSuperType(SuperType.LEGENDARY);
        this.getLeftHalfCard().setPT(new MageInt(2), new MageInt(3));

        // Flying
        this.getLeftHalfCard().addAbility(FlyingAbility.getInstance());

        // Vigilance
        this.getLeftHalfCard().addAbility(VigilanceAbility.getInstance());

        // Snow lands your opponents control enter the battlefield tapped.
        this.getLeftHalfCard().addAbility(new SimpleStaticAbility(new ReidaneGodOfTheWorthyTapEffect()));

        // Noncreature spells your opponents cast with converted mana cost 4 or more cost {2} more to cast.
        this.getLeftHalfCard().addAbility(new SimpleStaticAbility(new ReidaneGodOfTheWorthyCostEffect()));

        // 2.
        // Valkmira, Protector's Shield
        // Legendary Artifact
        this.getRightHalfCard().addSuperType(SuperType.LEGENDARY);

        // If a source an opponent controls would deal damage to you or a permanent you control, prevent 1 of that damage.
        this.getRightHalfCard().addAbility(new SimpleStaticAbility(new ValkmiraProtectorsShieldPreventionEffect()));

        // Whenever you or a permanent you control becomes the target of a spell or ability an opponent controls, counter that spell or ability unless its controller pays {1}.
        this.getRightHalfCard().addAbility(new ValkmiraProtectorsShieldTriggeredAbility());
    }

    private ReidaneGodOfTheWorthy(final ReidaneGodOfTheWorthy card) {
        super(card);
    }

    @Override
    public ReidaneGodOfTheWorthy copy() {
        return new ReidaneGodOfTheWorthy(this);
    }
}

class ReidaneGodOfTheWorthyTapEffect extends ReplacementEffectImpl {

    ReidaneGodOfTheWorthyTapEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Tap);
        staticText = "snow lands your opponents control enter the battlefield tapped";
    }

    private ReidaneGodOfTheWorthyTapEffect(final ReidaneGodOfTheWorthyTapEffect effect) {
        super(effect);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Permanent target = ((EntersTheBattlefieldEvent) event).getTarget();
        if (target != null) {
            target.setTapped(true);
        }
        return false;
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (!game.getOpponents(source.getControllerId()).contains(event.getPlayerId())) {
            return false;
        }
        Permanent permanent = ((EntersTheBattlefieldEvent) event).getTarget();
        return permanent != null && permanent.isLand() && permanent.isSnow();
    }

    @Override
    public ReidaneGodOfTheWorthyTapEffect copy() {
        return new ReidaneGodOfTheWorthyTapEffect(this);
    }
}

class ReidaneGodOfTheWorthyCostEffect extends CostModificationEffectImpl {

    ReidaneGodOfTheWorthyCostEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit, CostModificationType.INCREASE_COST);
        staticText = "Noncreature spells your opponents cast with converted mana cost 4 or greater cost {2} more to cast";
    }

    private ReidaneGodOfTheWorthyCostEffect(ReidaneGodOfTheWorthyCostEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source, Ability abilityToModify) {
        SpellAbility spellAbility = (SpellAbility) abilityToModify;
        CardUtil.adjustCost(spellAbility, -2);
        return true;
    }

    @Override
    public boolean applies(Ability abilityToModify, Ability source, Game game) {
        if ((!(abilityToModify instanceof SpellAbility))
                || !game.getOpponents(source.getControllerId()).contains(abilityToModify.getControllerId())) {
            return false;
        }
        Card spellCard = ((SpellAbility) abilityToModify).getCharacteristics(game);
        return spellCard != null && !spellCard.isCreature() && spellCard.getConvertedManaCost() >= 4;
    }

    @Override
    public ReidaneGodOfTheWorthyCostEffect copy() {
        return new ReidaneGodOfTheWorthyCostEffect(this);
    }
}

class ValkmiraProtectorsShieldPreventionEffect extends PreventionEffectImpl {

    ValkmiraProtectorsShieldPreventionEffect() {
        super(Duration.WhileOnBattlefield, 1, false, false);
        this.staticText = "If a source an opponent controls would deal damage to you or a permanent you control, prevent 1 of that damage";
    }

    private ValkmiraProtectorsShieldPreventionEffect(ValkmiraProtectorsShieldPreventionEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (!game.getOpponents(game.getControllerId(event.getSourceId())).contains(source.getControllerId())) {
            return false;
        }
        switch (event.getType()) {
            case DAMAGE_PLAYER:
                return source.isControlledBy(event.getTargetId())
                        && super.applies(event, source, game);
            case DAMAGE_CREATURE:
            case DAMAGE_PLANESWALKER:
                Permanent permanent = game.getPermanent(event.getTargetId());
                return permanent != null
                        && permanent.isControlledBy(source.getControllerId())
                        && super.applies(event, source, game);
        }
        return false;
    }

    @Override
    public ValkmiraProtectorsShieldPreventionEffect copy() {
        return new ValkmiraProtectorsShieldPreventionEffect(this);
    }
}

class ValkmiraProtectorsShieldTriggeredAbility extends TriggeredAbilityImpl {

    ValkmiraProtectorsShieldTriggeredAbility() {
        super(Zone.BATTLEFIELD, new CounterUnlessPaysEffect(new GenericManaCost(1)));
    }

    private ValkmiraProtectorsShieldTriggeredAbility(final ValkmiraProtectorsShieldTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public ValkmiraProtectorsShieldTriggeredAbility copy() {
        return new ValkmiraProtectorsShieldTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.TARGETED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        StackObject stackObject = game.getStack().getStackObject(event.getSourceId());
        if (stackObject == null || !game.getOpponents(getControllerId()).contains(stackObject.getControllerId())) {
            return false;
        }
        if (isControlledBy(event.getTargetId())) {
            this.getEffects().setTargetPointer(new FixedTarget(stackObject.getId(), game));
            return true;
        }
        Permanent permanent = game.getPermanent(event.getTargetId());
        if (permanent == null || !permanent.isControlledBy(getControllerId())) {
            return false;
        }
        this.getEffects().setTargetPointer(new FixedTarget(stackObject.getId(), game));
        return true;
    }

    @Override
    public String getRule() {
        return "Whenever you or a permanent you control becomes the target of a spell or ability " +
                "an opponent controls, counter that spell or ability unless its controller pays {1}.";
    }
}
