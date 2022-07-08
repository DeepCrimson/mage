package mage.server.tournament;

import mage.cards.decks.Deck;
import mage.server.User;
import mage.server.managers.ManagerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class TournamentSession {

    protected final UUID userId;
    protected final UUID playerId;
    protected final UUID tableId;
    protected final ScheduledExecutorService timeoutExecutor;
    private final ManagerFactory managerFactory;
    protected boolean killed = false;
    private ScheduledFuture<?> futureTimeout;

    public TournamentSession(ManagerFactory managerFactory, UUID userId, UUID tableId, UUID playerId) {
        this.managerFactory = managerFactory;
        this.timeoutExecutor = managerFactory.threadExecutor().getTimeoutExecutor();
        this.userId = userId;
        this.playerId = playerId;
        this.tableId = tableId;
    }

    public boolean init() {
        return false;
    }

    public void update() {
    }

    public void construct(int timeout) {
    }

    public void submitDeck(Deck deck) {
    }

    public boolean updateDeck(Deck deck) {
        return true;
    }

    public void setKilled() {
        killed = true;
    }

    public boolean isKilled() {
        return killed;
    }

    public void tournamentOver() {
        cleanUp();
        removeTournamentForUser();
    }

    public void quit() {
        cleanUp();
        removeTournamentForUser();
    }

    private void cleanUp() {
        if (futureTimeout != null && !futureTimeout.isDone()) {
            futureTimeout.cancel(true);
        }
    }

    private void removeTournamentForUser() {
        Optional<User> user = managerFactory.userManager().getUser(userId);
        if (user.isPresent()) {
            user.get().removeTable(playerId);
            user.get().removeTournament(playerId);
        }
    }

}
