package mage.server.record;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.DatabaseConnection;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public enum TableRecordRepository {

    instance;

    private static final String JDBC_URL = "jdbc:sqlite:./db/table_record.db";
    private static final String VERSION_ENTITY_NAME = "table_record";
    // raise this if db structure was changed
    private static final long DB_VERSION = 0;

    private Dao<TableRecord, Object> dao;

    TableRecordRepository() {
    }

    public void add(TableRecord tableHistory) {
        try {
            dao.create(tableHistory);
        } catch (SQLException ex) {
        }
    }

    public List<TableRecord> getAfter(long endTimeMs) {
        try {
            QueryBuilder<TableRecord, Object> qb = dao.queryBuilder();
            qb.where().gt("endTimeMs", new SelectArg(endTimeMs));
            qb.orderBy("endTimeMs", true);
            return dao.query(qb.prepare());
        } catch (SQLException ex) {
        }
        return Collections.emptyList();
    }

    public void closeDB() {
        try {
            if (dao != null && dao.getConnectionSource() != null) {
                DatabaseConnection conn = dao.getConnectionSource().getReadWriteConnection(dao.getTableName());
                conn.executeStatement("shutdown compact", 0);
            }
        } catch (SQLException ex) {
        }
    }
}
