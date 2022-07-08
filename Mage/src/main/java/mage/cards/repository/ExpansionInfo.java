package mage.cards.repository;

import mage.cards.ExpansionSet;
import mage.constants.SetType;

import java.util.Date;

/**
 * @author North
 */
public class ExpansionInfo {

    protected String name;
    protected String code;
    protected String blockName;
    protected Date releaseDate;
    protected SetType type;
    protected boolean boosters;
    protected boolean basicLands;

    public ExpansionInfo() {
    }

    public ExpansionInfo(ExpansionSet expansionSet) {
        this.name = expansionSet.getName();
        this.code = expansionSet.getCode();
        this.blockName = expansionSet.getBlockName();
        this.releaseDate = expansionSet.getReleaseDate();
        this.type = expansionSet.getSetType();
        this.boosters = expansionSet.hasBoosters();
        this.basicLands = expansionSet.hasBasicLands();
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getBlockName() {
        return blockName;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public SetType getType() {
        return type;
    }

    public boolean hasBoosters() {
        return boosters;
    }

    public boolean hasBasicLands() {
        return basicLands;
    }

    @Override
    public String toString() {
        return name;
    }
}
