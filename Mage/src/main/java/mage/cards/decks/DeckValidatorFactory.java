package mage.cards.decks;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author BetaSteward_at_googlemail.com
 */
public enum DeckValidatorFactory {

    instance;

    private final Map<String, Class> deckTypes = new LinkedHashMap<>();

    public DeckValidator createDeckValidator(String deckType) {

        DeckValidator validator;
        try {
            Constructor<?> con = deckTypes.get(deckType).getConstructor();
            validator = (DeckValidator) con.newInstance();
        } catch (Exception ex) {
            return null;
        }

        return validator;
    }

    public Set<String> getDeckTypes() {
        return deckTypes.keySet();
    }

    public void addDeckType(String name, Class deckType) {
        if (deckType != null) {
            this.deckTypes.put(name, deckType);
        }
    }

}
