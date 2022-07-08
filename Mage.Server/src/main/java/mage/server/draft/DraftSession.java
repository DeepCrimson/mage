package mage.server.draft;

import mage.server.managers.ManagerFactory;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class DraftSession {

    protected final UUID userId;
    protected final UUID playerId;
    protected final ScheduledExecutorService timeoutExecutor;
    private final ManagerFactory managerFactory;
    protected boolean killed = false;
    protected UUID markedCard;
    private ScheduledFuture<?> futureTimeout;

    public DraftSession(ManagerFactory managerFactory, UUID userId, UUID playerId) {
        this.managerFactory = managerFactory;
        this.timeoutExecutor = managerFactory.threadExecutor().getTimeoutExecutor();
        this.userId = userId;
        this.playerId = playerId;
        this.markedCard = null;
    }

    public boolean init() {
        return false;
    }

    public void update() {
    }

    public void draftOver() {
    }

    public void pickCard(int timeout) {
    }

    private synchronized void cancelTimeout() {
        if (futureTimeout != null) {
            futureTimeout.cancel(false);
        }
    }

    public void setKilled() {
        killed = true;
    }


    public void removeDraft() {
        managerFactory.userManager().getUser(userId).ifPresent(user -> user.removeDraft(playerId));

    }

    public UUID getDraftId() {
        return null;
    }

    public UUID getMarkedCard() {
        return markedCard;
    }

    public void setMarkedCard(UUID markedCard) {
        this.markedCard = markedCard;
    }

}
