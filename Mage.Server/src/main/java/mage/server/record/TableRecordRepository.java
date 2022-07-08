package mage.server.record;

import java.util.Collections;
import java.util.List;

public enum TableRecordRepository {

    instance;

    private static final String JDBC_URL = "jdbc:sqlite:./db/table_record.db";
    private static final String VERSION_ENTITY_NAME = "table_record";
    // raise this if db structure was changed
    private static final long DB_VERSION = 0;


    TableRecordRepository() {
    }

    public void add(TableRecord tableHistory) {
    }

    public List<TableRecord> getAfter(long endTimeMs) {
        return Collections.emptyList();
    }

    public void closeDB() {
    }
}
