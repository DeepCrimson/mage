package mage.cards.repository;

import mage.constants.CardType;
import mage.constants.Rarity;
import mage.constants.SubType;
import mage.constants.SuperType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author North
 */
public class CardCriteria {

    private String name;
    private String nameExact;
    private String rules;
    private final List<String> setCodes;
    private final List<String> ignoreSetCodes; // sets to ignore, use with little amount of sets (example: ignore sets with snow lands)
    private final List<CardType> types;
    private final List<CardType> notTypes;
    private final List<SuperType> supertypes;
    private final List<SuperType> notSupertypes;
    private final List<SubType> subtypes;
    private final List<Rarity> rarities;
    private Boolean doubleFaced;
    private Boolean modalDoubleFaced;
    private boolean black;
    private boolean blue;
    private boolean green;
    private boolean red;
    private boolean white;
    private boolean colorless;
    private Integer manaValue;
    private String sortBy;
    private Long start;
    private Long count;
    // compare numerical card numbers (123b -> 123)
    private int minCardNumber;
    private int maxCardNumber;

    public CardCriteria() {
        this.setCodes = new ArrayList<>();
        this.ignoreSetCodes = new ArrayList<>();
        this.rarities = new ArrayList<>();
        this.types = new ArrayList<>();
        this.notTypes = new ArrayList<>();
        this.supertypes = new ArrayList<>();
        this.notSupertypes = new ArrayList<>();
        this.subtypes = new ArrayList<>();

        this.black = true;
        this.blue = true;
        this.green = true;
        this.red = true;
        this.white = true;
        this.colorless = true;

        this.minCardNumber = Integer.MIN_VALUE;
        this.maxCardNumber = Integer.MAX_VALUE;
    }

    public CardCriteria black(boolean black) {
        this.black = black;
        return this;
    }

    public CardCriteria blue(boolean blue) {
        this.blue = blue;
        return this;
    }

    public CardCriteria green(boolean green) {
        this.green = green;
        return this;
    }

    public CardCriteria red(boolean red) {
        this.red = red;
        return this;
    }

    public CardCriteria white(boolean white) {
        this.white = white;
        return this;
    }

    public CardCriteria colorless(boolean colorless) {
        this.colorless = colorless;
        return this;
    }

    public CardCriteria doubleFaced(boolean doubleFaced) {
        this.doubleFaced = doubleFaced;
        return this;
    }

    public CardCriteria modalDoubleFaced(boolean modalDoubleFaced) {
        this.modalDoubleFaced = modalDoubleFaced;
        return this;
    }

    public CardCriteria name(String name) {
        this.name = name;
        return this;
    }

    public CardCriteria nameExact(String nameExact) {
        this.nameExact = nameExact;
        return this;
    }

    public CardCriteria rules(String rules) {
        this.rules = rules;
        return this;
    }

    public CardCriteria start(Long start) {
        this.start = start;
        return this;
    }

    public CardCriteria count(Long count) {
        this.count = count;
        return this;
    }

    public CardCriteria rarities(Rarity... rarities) {
        this.rarities.addAll(Arrays.asList(rarities));
        return this;
    }

    public CardCriteria setCodes(String... setCodes) {
        this.setCodes.addAll(Arrays.asList(setCodes));
        return this;
    }

    public CardCriteria ignoreSetCodes(String... ignoreSetCodes) {
        this.ignoreSetCodes.addAll(Arrays.asList(ignoreSetCodes));
        return this;
    }

    public CardCriteria ignoreSetsWithSnowLands() {
        this.ignoreSetCodes.addAll(CardRepository.snowLandSetCodes);
        return this;
    }

    public CardCriteria types(CardType... types) {
        this.types.addAll(Arrays.asList(types));
        return this;
    }

    public CardCriteria notTypes(CardType... types) {
        this.notTypes.addAll(Arrays.asList(types));
        return this;
    }

    public CardCriteria supertypes(SuperType... supertypes) {
        this.supertypes.addAll(Arrays.asList(supertypes));
        return this;
    }

    public CardCriteria notSupertypes(SuperType... supertypes) {
        this.notSupertypes.addAll(Arrays.asList(supertypes));
        return this;
    }

    public CardCriteria subtypes(SubType... subtypes) {
        this.subtypes.addAll(Arrays.asList(subtypes));
        return this;
    }

    public CardCriteria manaValue(Integer manaValue) {
        this.manaValue = manaValue;
        return this;
    }

    public CardCriteria minCardNumber(int minCardNumber) {
        this.minCardNumber = minCardNumber;
        return this;
    }

    public CardCriteria maxCardNumber(int maxCardNumber) {
        this.maxCardNumber = maxCardNumber;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getRules() {
        return rules;
    }

    public List<String> getSetCodes() {
        return setCodes;
    }

    public List<String> getIgnoreSetCodes() {
        return ignoreSetCodes;
    }

    public List<CardType> getTypes() {
        return types;
    }

    public List<CardType> getNotTypes() {
        return notTypes;
    }

    public List<SuperType> getSupertypes() {
        return supertypes;
    }

    public List<SuperType> getNotSupertypes() {
        return notSupertypes;
    }

    public List<SubType> getSubtypes() {
        return subtypes;
    }

    public List<Rarity> getRarities() {
        return rarities;
    }

    public Boolean getDoubleFaced() {
        return doubleFaced;
    }

    public Boolean getModalDoubleFaced() {
        return modalDoubleFaced;
    }

    public boolean isBlack() {
        return black;
    }

    public boolean isBlue() {
        return blue;
    }

    public boolean isGreen() {
        return green;
    }

    public boolean isRed() {
        return red;
    }

    public boolean isWhite() {
        return white;
    }

    public boolean isColorless() {
        return colorless;
    }

    public Integer getManaValue() {
        return manaValue;
    }

    public String getSortBy() {
        return sortBy;
    }

    public Long getStart() {
        return start;
    }

    public Long getCount() {
        return count;
    }

    public int getMinCardNumber() {
        return minCardNumber;
    }

    public int getMaxCardNumber() {
        return maxCardNumber;
    }

}
