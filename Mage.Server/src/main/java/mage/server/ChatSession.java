package mage.server;

import mage.game.Game;
import mage.server.managers.ManagerFactory;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class ChatSession {
    private static final DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT);

    private final ManagerFactory managerFactory;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final ConcurrentMap<UUID, String> clients = new ConcurrentHashMap<>();
    private final UUID chatId;
    private final Date createTime;
    private final String info;

    public ChatSession(ManagerFactory managerFactory, String info) {
        this.managerFactory = managerFactory;
        chatId = UUID.randomUUID();
        this.createTime = new Date();
        this.info = info;
    }

    public void join(UUID userId) {
        managerFactory.userManager().getUser(userId).ifPresent(user -> {
            if (!clients.containsKey(userId)) {
                String userName = user.getName();
                final Lock w = lock.writeLock();
                w.lock();
                try {
                    clients.put(userId, userName);
                } finally {
                    w.unlock();
                }
            }
        });
    }

    public void kill(UUID userId, DisconnectReason reason) {

        try {
            if (reason == null) {
                reason = DisconnectReason.Undefined;
            }
            if (userId != null && clients.containsKey(userId)) {
                String userName = clients.get(userId);
                if (reason != DisconnectReason.LostConnection) { // for lost connection the user will be reconnected or session expire so no removeUserFromAllTablesAndChat of chat yet
                    final Lock w = lock.writeLock();
                    w.lock();
                    try {
                        clients.remove(userId);
                    } finally {
                        w.unlock();
                    }
                }
                String message = reason.getMessage();

                if (!message.isEmpty()) {
                    broadcast(null, userName + message, true, null, null);
                }
            }
        } catch (Exception ex) {
        }
    }

    public boolean broadcastInfoToUser(User toUser, String message) {
        return false;
    }

    public boolean broadcastWhisperToUser(User fromUser, User toUser, String message) {
        return false;
    }

    public void broadcast(String userName, String message, boolean withTime, Game game) {
    }

    /**
     * @return the chatId
     */
    public UUID getChatId() {
        return chatId;
    }

    public boolean hasUser(UUID userId) {
        return clients.containsKey(userId);
    }

    public ConcurrentMap<UUID, String> getClients() {
        return clients;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public String getInfo() {
        return info;
    }

}
