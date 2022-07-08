package mage.cards.repository;

/**
 * @author JayDi85
 */
public class DatabaseBuild {

    protected String entity;

    protected String lastBuild;

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getLastBuild() {
        return lastBuild;
    }

    public void setLastBuild(String lastBuild) {
        this.lastBuild = lastBuild;
    }
}
