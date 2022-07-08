package mage.server.draft;

import mage.MageException;
import mage.players.Player;
import mage.server.managers.ManagerFactory;
import mage.view.DraftPickView;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class DraftController {

    private final ManagerFactory managerFactory;
    private final ConcurrentMap<UUID, DraftSession> draftSessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, UUID> userPlayerMap;
    private final UUID draftSessionId;
    private final UUID tableId;

    public DraftController(ManagerFactory managerFactory, ConcurrentHashMap<UUID, UUID> userPlayerMap, UUID tableId) {
        this.managerFactory = managerFactory;
        draftSessionId = UUID.randomUUID();
        this.userPlayerMap = userPlayerMap;
        this.tableId = tableId;
        init();
    }

    private void init() {
    }

    private UUID getPlayerId(UUID userId) {
        return userPlayerMap.get(userId);
    }

    public void join(UUID userId) {
    }

    public Optional<DraftSession> getDraftSession(UUID playerId) {
        if (draftSessions.containsKey(playerId)) {
            return Optional.of(draftSessions.get(playerId));
        }
        return Optional.empty();
    }

    public boolean replacePlayer(Player oldPlayer, Player newPlayer) {
        return false;
    }

    private void leave(UUID userId) {
    }

    private void endDraft() throws MageException {
        for (final DraftSession draftSession : draftSessions.values()) {
            draftSession.draftOver();
            draftSession.removeDraft();
        }
    }

    public void kill(UUID userId) {
        if (userPlayerMap.containsKey(userId)) {
            draftSessions.get(userPlayerMap.get(userId)).setKilled();
            draftSessions.remove(userPlayerMap.get(userId));
            leave(userId);
            userPlayerMap.remove(userId);
        }
    }

    public void timeout(UUID userId) {
        if (userPlayerMap.containsKey(userId)) {
            DraftSession draftSession = draftSessions.get(userPlayerMap.get(userId));
            if (draftSession != null) {
                UUID cardId = draftSession.getMarkedCard();
                if (cardId != null) {
                    sendCardPick(userId, cardId, null);
                    return;
                }
            }
        }
    }

    public UUID getSessionId() {
        return this.draftSessionId;
    }

    public DraftPickView sendCardPick(UUID userId, UUID cardId, Set<UUID> hiddenCards) {
        DraftSession draftSession = draftSessions.get(userPlayerMap.get(userId));
        if (draftSession != null) {
            draftSession.setMarkedCard(null);
        }
        return null;
    }

    public void sendCardMark(UUID userId, UUID cardId) {
        draftSessions.get(userPlayerMap.get(userId)).setMarkedCard(cardId);
    }

    private synchronized void updateDraft() throws MageException {
        for (final Entry<UUID, DraftSession> entry : draftSessions.entrySet()) {
            entry.getValue().update();
        }
    }

    private synchronized void pickCard(UUID playerId, int timeout) throws MageException {
        if (draftSessions.containsKey(playerId)) {
            draftSessions.get(playerId).pickCard(timeout);
        }
    }

    public UUID getTableId() {
        return tableId;
    }

    public void abortDraft() {
    }
}
