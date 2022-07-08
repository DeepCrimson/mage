package mage.cards.repository;

/**
 * @author North
 */
public class DatabaseVersion {


    protected String entity;


    protected Long version;

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
