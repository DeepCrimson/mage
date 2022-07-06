package mage.game.events;

import mage.cards.Cards;
import mage.cards.decks.Deck;
import mage.game.Game;
import mage.game.events.TableEvent.EventType;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class TableEventSource implements EventSource<TableEvent>, Serializable {

    protected final EventDispatcher<TableEvent> dispatcher = new EventDispatcher<TableEvent>() {
    };

    @Override
    public void addListener(Listener<TableEvent> listener) {
        dispatcher.addListener(listener);
    }

    @Override
    public void removeAllListener() {
        dispatcher.removeAllListener();
    }


    public void fireTableEvent(EventType eventType) {
        dispatcher.fireEvent(new TableEvent(eventType));
    }

    public void fireTableEvent(EventType eventType, String message, Game game) {
        dispatcher.fireEvent(new TableEvent(eventType, message, game));
    }

    public void fireTableEvent(EventType eventType, String message, boolean withTime, boolean withTurnInfo, Game game) {
        dispatcher.fireEvent(new TableEvent(eventType, message, withTime, withTurnInfo, game));
    }

    public void fireTableEvent(EventType eventType, UUID playerId, String message, Game game) {
        dispatcher.fireEvent(new TableEvent(eventType, playerId, message, game));
    }

    public void fireTableEvent(EventType eventType, String message, Exception ex, Game game) {
        dispatcher.fireEvent(new TableEvent(eventType, message, ex, game));
    }

    public void fireTableEvent(EventType eventType, String message, Cards cards, Game game) {
        dispatcher.fireEvent(new TableEvent(eventType, message, cards, game));
    }

    public void fireTableEvent(EventType eventType, UUID playerId, Deck deck, int timeout) {
        dispatcher.fireEvent(new TableEvent(eventType, playerId, deck, timeout));
    }
}
