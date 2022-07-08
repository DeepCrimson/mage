package mage.server.draft;

import mage.MageException;
import mage.game.draft.Draft;
import mage.players.Player;
import mage.server.game.GameController;
import mage.server.managers.ManagerFactory;
import mage.view.DraftPickView;
import org.apache.log4j.Logger;

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

    private static final Logger logger = Logger.getLogger(GameController.class);

    private final ManagerFactory managerFactory;
    private final ConcurrentMap<UUID, DraftSession> draftSessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, UUID> userPlayerMap;
    private final UUID draftSessionId;
    private final Draft draft;
    private final UUID tableId;

    public DraftController(ManagerFactory managerFactory, Draft draft, ConcurrentHashMap<UUID, UUID> userPlayerMap, UUID tableId) {
        this.managerFactory = managerFactory;
        draftSessionId = UUID.randomUUID();
        this.userPlayerMap = userPlayerMap;
        this.draft = draft;
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
        draft.leave(getPlayerId(userId));
    }

    private void endDraft() throws MageException {
        for (final DraftSession draftSession : draftSessions.values()) {
            draftSession.draftOver();
            draftSession.removeDraft();
        }
        managerFactory.tableManager().endDraft(tableId, draft);
        managerFactory.draftManager().removeDraft(draft.getId());
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
            draft.autoPick(userPlayerMap.get(userId));
            logger.debug("Draft pick timeout - autopick for player: " + userPlayerMap.get(userId));
        }
    }

    public UUID getSessionId() {
        return this.draftSessionId;
    }

    public DraftPickView sendCardPick(UUID userId, UUID cardId, Set<UUID> hiddenCards) {
        DraftSession draftSession = draftSessions.get(userPlayerMap.get(userId));
        if (draftSession != null) {
            draftSession.setMarkedCard(null);
            return draftSession.sendCardPick(cardId, hiddenCards);
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
        draft.setAbort(true);
        try {
            endDraft();
        } catch (MageException ex) {

        }
    }
}
