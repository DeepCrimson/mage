package mage.server.console;

import mage.remote.Session;
import mage.view.TableView;
import mage.view.UserView;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import static javax.swing.JTable.AUTO_RESIZE_NEXT_COLUMN;
import static javax.swing.JTable.AUTO_RESIZE_OFF;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class ConsolePanel extends javax.swing.JPanel {

    private final TableUserModel tableUserModel;
    private final TableTableModel tableTableModel;
    private UpdateUsersTask updateUsersTask;
    private UpdateTablesTask updateTablesTask;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDeActivate;
    private javax.swing.JButton btnDisconnect;
    private javax.swing.JButton btnEndSession;
    private javax.swing.JButton btnLockUser;
    private javax.swing.JButton btnMuteUser;
    private javax.swing.JButton btnRemoveTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextField jUserName;
    private javax.swing.JLabel lblMinutes;
    private javax.swing.JSpinner spinnerMuteDurationMinutes;
    private javax.swing.JTable tblTables;
    private javax.swing.JTable tblUsers;

    /**
     * Creates new form ConsolePanel
     */
    public ConsolePanel() {
        this.tableUserModel = new TableUserModel();
        this.tableTableModel = new TableTableModel();
        initComponents();
        spinnerMuteDurationMinutes.setValue(60);
        this.tblUsers.createDefaultColumnsFromModel();
        this.tblUsers.setRowSorter(new TableRowSorter(tableUserModel));
        this.tblUsers.setAutoResizeMode(AUTO_RESIZE_OFF);

        this.tblTables.createDefaultColumnsFromModel();
        this.tblTables.setRowSorter(new TableRowSorter(tableTableModel));
        this.tblUsers.setAutoResizeMode(AUTO_RESIZE_NEXT_COLUMN);
    }

    public void update(List<UserView> users) {
        int row = this.tblUsers.getSelectedRow();
        tableUserModel.loadData(users);
        this.tblUsers.repaint();
        this.tblUsers.getSelectionModel().setSelectionInterval(row, row);
    }

    public void update(Collection<TableView> tables) {
        int row = this.tblTables.getSelectedRow();
        tableTableModel.loadData(tables);
        this.tblTables.repaint();
        this.tblTables.getSelectionModel().setSelectionInterval(row, row);
    }

    public void start() {
        updateUsersTask = new UpdateUsersTask(ConsoleFrame.getSession(), this);
        updateTablesTask = new UpdateTablesTask(ConsoleFrame.getSession(), ConsoleFrame.getSession().getMainRoomId(), this);
        updateUsersTask.execute();
        updateTablesTask.execute();
    }

    public void stop() {
        if (updateUsersTask != null && !updateUsersTask.isDone()) {
            updateUsersTask.cancel(true);
        }
        if (updateTablesTask != null && !updateTablesTask.isDone()) {
            updateTablesTask.cancel(true);
        }
    }

    public JTextField getjUserName() {
        return jUserName;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblUsers = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        btnDisconnect = new javax.swing.JButton();
        btnEndSession = new javax.swing.JButton();
        btnMuteUser = new javax.swing.JButton();
        btnDeActivate = new javax.swing.JButton();
        btnLockUser = new javax.swing.JButton();
        lblMinutes = new javax.swing.JLabel();
        spinnerMuteDurationMinutes = new javax.swing.JSpinner();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblTables = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        btnRemoveTable = new javax.swing.JButton();
        jUserName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        jSplitPane1.setDividerLocation(250);
        jSplitPane1.setResizeWeight(0.5);

        tblUsers.setModel(tableUserModel);
        jScrollPane1.setViewportView(tblUsers);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE)
        );

        jPanel4.setVerifyInputWhenFocusTarget(false);

        btnDisconnect.setText("Disconnect");
        btnDisconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDisconnectActionPerformed(evt);
            }
        });

        btnEndSession.setText("End session");
        btnEndSession.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEndSessionActionPerformed(evt);
            }
        });

        btnMuteUser.setText("Mute user");
        btnMuteUser.setActionCommand("Mute 1h");
        btnMuteUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMuteUserActionPerformed(evt);
            }
        });

        btnDeActivate.setText("(de)activate");
        btnDeActivate.setActionCommand("Mute 1h");
        btnDeActivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeActivateActionPerformed(evt);
            }
        });

        btnLockUser.setText("Lock user");
        btnLockUser.setActionCommand("Mute 1h");
        btnLockUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLockUserActionPerformed(evt);
            }
        });

        lblMinutes.setText("Minutes");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(btnDisconnect)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addComponent(btnEndSession)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnMuteUser))
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addComponent(btnDeActivate)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnLockUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblMinutes)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(spinnerMuteDurationMinutes, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(btnDisconnect)
                                                        .addComponent(btnEndSession)
                                                        .addComponent(btnMuteUser))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(btnDeActivate)
                                                        .addComponent(btnLockUser)))
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addGap(16, 16, 16)
                                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(lblMinutes)
                                                        .addComponent(spinnerMuteDurationMinutes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(0, 0, 0)
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0))
        );

        jSplitPane1.setLeftComponent(jPanel1);

        tblTables.setModel(tableTableModel);
        jScrollPane2.setViewportView(tblTables);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 606, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
        );

        btnRemoveTable.setText("Remove Table");
        btnRemoveTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveTableActionPerformed(evt);
            }
        });

        jUserName.setName("Username"); // NOI18N

        jLabel1.setText("Username:");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
                jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jUserName, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnRemoveTable)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
                jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                                .addContainerGap(20, Short.MAX_VALUE)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnRemoveTable)
                                        .addComponent(jUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel1))
                                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane1.setRightComponent(jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnDisconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisconnectActionPerformed
        int row = this.tblUsers.convertRowIndexToModel(tblUsers.getSelectedRow());
        ConsoleFrame.getSession().disconnectUser((String) tableUserModel.getValueAt(row, TableUserModel.POS_SESSION_ID));
    }//GEN-LAST:event_btnDisconnectActionPerformed

    private void btnEndSessionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEndSessionActionPerformed
        int row = this.tblUsers.convertRowIndexToModel(tblUsers.getSelectedRow());
        String userSessionId = (String) tableUserModel.getValueAt(row, TableUserModel.POS_GAME_INFO);

        if (JOptionPane.showConfirmDialog(null, "Are you sure you mean to end userSessionId " + userSessionId + '?', "WARNING",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            ConsoleFrame.getSession().endUserSession(userSessionId);
        }
    }//GEN-LAST:event_btnEndSessionActionPerformed

    private void btnMuteUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMuteUserActionPerformed
        int row = this.tblUsers.convertRowIndexToModel(tblUsers.getSelectedRow());
        String userName = (String) tableUserModel.getValueAt(row, TableUserModel.POS_USER_NAME);
        long durationMinute = ((Number) spinnerMuteDurationMinutes.getValue()).longValue();
        if (JOptionPane.showConfirmDialog(null, "Are you sure you mean to mute user: " + userName + " for " + durationMinute + " minutes?", "WARNING",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            ConsoleFrame.getSession().muteUserChat(userName, durationMinute);
        }
    }//GEN-LAST:event_btnMuteUserActionPerformed

    private void btnDeActivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeActivateActionPerformed
        String userName;
        if (!getjUserName().getText().isEmpty()) {
            userName = getjUserName().getText();
        } else {
            int row = this.tblUsers.convertRowIndexToModel(tblUsers.getSelectedRow());
            userName = (String) tableUserModel.getValueAt(row, TableUserModel.POS_USER_NAME);
        }

        if (JOptionPane.showConfirmDialog(null, "Did you want to set user: " + userName + " to active?", "WARNING",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            ConsoleFrame.getSession().setActivation(userName, true);
            return;
        }
        if (JOptionPane.showConfirmDialog(null, "Did you want to set user: " + userName + " to inactive?", "WARNING",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            ConsoleFrame.getSession().setActivation(userName, false);
            return;
        }
        if (JOptionPane.showConfirmDialog(null, "Are you sure you mean to toggle activation for user: " + userName + '?', "WARNING",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            ConsoleFrame.getSession().toggleActivation(userName);
            return;
        }
    }//GEN-LAST:event_btnDeActivateActionPerformed

    private void btnLockUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLockUserActionPerformed
        int row = this.tblUsers.convertRowIndexToModel(tblUsers.getSelectedRow());
        String userName = (String) tableUserModel.getValueAt(row, TableUserModel.POS_USER_NAME);
        long durationMinute = ((Number) spinnerMuteDurationMinutes.getValue()).longValue();
        if (JOptionPane.showConfirmDialog(null, "Are you sure you mean to lock user: " + userName + " for " + durationMinute + " minutes?", "WARNING",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            ConsoleFrame.getSession().lockUser(userName, durationMinute);
        }
    }//GEN-LAST:event_btnLockUserActionPerformed

    private void btnRemoveTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveTableActionPerformed
        int row = this.tblTables.convertRowIndexToModel(tblTables.getSelectedRow());
        ConsoleFrame.getSession().removeTable((UUID) tableTableModel.getValueAt(row, 7));
    }//GEN-LAST:event_btnRemoveTableActionPerformed
    // End of variables declaration//GEN-END:variables
}

class TableUserModel extends AbstractTableModel {

    public static final int POS_USER_NAME = 0;
    public static final int POS_HOST = 1;
    public static final int POS_TIME_CONNECTED = 2;
    public static final int POS_LAST_ACTIVITY = 3;
    public static final int POS_SESSION_ID = 4;
    public static final int POS_GAME_INFO = 5;
    public static final int POS_USER_STATE = 6;
    public static final int POS_CHAT_MUTE = 7;
    public static final int POS_CLIENT_VERSION = 8;
    private static final DateFormat formatterTime = new SimpleDateFormat("HH:mm:ss");
    private static final DateFormat formatterTimeStamp = new SimpleDateFormat("yy-M-dd HH:mm:ss");
    private final String[] columnNames = new String[]{"User Name", "Host", "Time Connected", "Last activity", "SessionId", "Gameinfo", "User state", "Chat mute", "Client Version"};
    private UserView[] users = new UserView[0];

    public void loadData(List<UserView> users) {
        this.users = users.toArray(new UserView[0]);
        this.fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return users.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int arg0, int arg1) {
        switch (arg1) {
            case POS_USER_NAME:
                return users[arg0].getUserName();
            case POS_HOST:
                return users[arg0].getHost();
            case POS_TIME_CONNECTED:
                return formatterTime.format(users[arg0].getTimeConnected());
            case POS_LAST_ACTIVITY:
                return formatterTime.format(users[arg0].getLastActivity());
            case POS_SESSION_ID:
                return users[arg0].getSessionId();
            case POS_GAME_INFO:
                return users[arg0].getGameInfo();
            case POS_USER_STATE:
                return users[arg0].getUserState();
            case POS_CHAT_MUTE:
                if (users[arg0].getMuteChatUntil() == null) {
                    return "";
                }
                return formatterTimeStamp.format(users[arg0].getMuteChatUntil());
            case POS_CLIENT_VERSION:
                return users[arg0].getClientVersion();
        }
        return "";
    }

    @Override
    public String getColumnName(int columnIndex) {
        String colName = "";

        if (columnIndex <= getColumnCount()) {
            colName = columnNames[columnIndex];
        }

        return colName;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

}

class TableTableModel extends AbstractTableModel {

    private final String[] columnNames = new String[]{"Table Name", "Owner", "Game Type", "Deck Type", "Status"};
    private TableView[] tables = new TableView[0];

    public void loadData(Collection<TableView> tables) {
        this.tables = tables.toArray(new TableView[0]);
        this.fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return tables.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int arg0, int arg1) {
        switch (arg1) {
            case 0:
                return tables[arg0].getTableName();
            case 1:
                return tables[arg0].getControllerName();
            case 2:
                return tables[arg0].getGameType();
            case 3:
                return tables[arg0].getDeckType();
            case 4:
                return tables[arg0].getTableState().toString();
            case 5:
                return tables[arg0].isTournament();
            case 6:
                if (!tables[arg0].getGames().isEmpty()) {
                    return tables[arg0].getGames().get(0);
                }
                return null;
            case 7:
                return tables[arg0].getTableId();
        }
        return "";
    }

    @Override
    public String getColumnName(int columnIndex) {
        String colName = "";

        if (columnIndex <= getColumnCount()) {
            colName = columnNames[columnIndex];
        }

        return colName;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 5;
    }

}

class UpdateUsersTask extends SwingWorker<Void, List<UserView>> {
    private final Session session;
    private final ConsolePanel panel;
    Map<String, String> peopleIps = new HashMap<>();
    private List<UserView> previousUsers;

    UpdateUsersTask(Session session, ConsolePanel panel) {
        this.session = session;
        this.panel = panel;
    }

    @Override
    protected Void doInBackground() throws Exception {
        while (!isCancelled()) {
            List<UserView> users = session.getUsers();
            if (!panel.getjUserName().getText().equals("")) {
                List<UserView> users2 = new ArrayList<>();
                for (UserView user : users) {
                    if (user.getUserName().toUpperCase(Locale.ENGLISH).matches(".*" + panel.getjUserName().getText().toUpperCase(Locale.ENGLISH) + ".*")) {
                        users2.add(user);
                    }
                }
                users = users2;
            }

            checkUserListChanged(users);
            this.publish(users);
            previousUsers = users;
            Thread.sleep(2000);
        }
        return null;
    }

    private void checkUserListChanged(List<UserView> usersToCheck) {
        if (previousUsers == null || usersToCheck == null) {
            return;
        }

        for (UserView u1 : previousUsers) {
            String s = u1.getUserName() + ',' + u1.getHost();
            if (peopleIps.get(s) == null) {
                peopleIps.put(s, "1");
            }
        }

        for (UserView u1 : usersToCheck) {
            String s = u1.getUserName() + ',' + u1.getHost();
            if (peopleIps.get(s) == null) {
                peopleIps.put(s, "1");
            }
        }
    }

    @Override
    protected void process(List<List<UserView>> view) {
        panel.update(view.get(0));
    }

    public ConsolePanel getPanel() {
        return panel;
    }

    @Override
    protected void done() {
        try {
            get();
        } catch (InterruptedException ex) {
        } catch (ExecutionException ex) {
        } catch (CancellationException ex) {
        }
    }
}

class UpdateTablesTask extends SwingWorker<Void, Collection<TableView>> {
    private final Session session;
    private final UUID roomId;
    private final ConsolePanel panel;

    UpdateTablesTask(Session session, UUID roomId, ConsolePanel panel) {
        this.session = session;
        this.roomId = roomId;
        this.panel = panel;
    }

    @Override
    protected Void doInBackground() throws Exception {
        while (!isCancelled()) {
            Collection<TableView> tableViews = session.getTables(roomId);
            if (!panel.getjUserName().getText().equals("")) {
                Collection<TableView> tableViews2 = new ArrayList<>();
                for (TableView table : tableViews) {
                    if (table.getControllerName().toUpperCase(Locale.ENGLISH).matches(".*" + panel.getjUserName().getText().toUpperCase(Locale.ENGLISH) + ".*")) {
                        tableViews2.add(table);
                    }
                }
                tableViews = tableViews2;
            }

            this.publish(tableViews);
            Thread.sleep(3000);
        }
        return null;
    }

    @Override
    protected void process(List<Collection<TableView>> view) {
        panel.update(view.get(0));
    }

    @Override
    protected void done() {
        try {
            get();
        } catch (InterruptedException ex) {
        } catch (ExecutionException ex) {
        } catch (CancellationException ex) {
        }
    }
}
