package mage.server.record;

import mage.game.Table.TableRecorder;
import mage.server.managers.UserManager;

public class TableRecorderImpl implements TableRecorder {

    private final UserManager userManager;

    public TableRecorderImpl(UserManager userManager) {
        this.userManager = userManager;
    }
}
