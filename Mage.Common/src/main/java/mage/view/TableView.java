package mage.view;

import mage.constants.TableState;
import mage.game.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class TableView implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID tableId;
    private String gameType;
    private String tableName;
    private String controllerName;
    private Date createTime;
    private TableState tableState;
    private boolean isTournament;
    private List<SeatView> seats = new ArrayList<>();
    private List<UUID> games = new ArrayList<>();

    public TableView(Table table) {
    }

    public UUID getTableId() {
        return tableId;
    }

    public String getTableName() {
        return tableName;
    }

    public String getControllerName() {
        return controllerName;
    }


    public String getGameType() {
        return gameType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public TableState getTableState() {
        return tableState;
    }

    public List<SeatView> getSeats() {
        return seats;
    }

    public List<UUID> getGames() {
        return games;
    }

    public boolean isTournament() {
        return this.isTournament;
    }
}
