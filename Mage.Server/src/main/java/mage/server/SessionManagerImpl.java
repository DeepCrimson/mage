package mage.server;

import mage.MageException;
import mage.players.net.UserData;
import mage.server.managers.ManagerFactory;
import mage.server.managers.SessionManager;
import org.jboss.remoting.callback.InvokerCallbackHandler;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class SessionManagerImpl implements SessionManager {

    private final ManagerFactory managerFactory;
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public SessionManagerImpl(ManagerFactory managerFactory) {
        this.managerFactory = managerFactory;
    }

    @Override
    public Optional<Session> getSession(@Nonnull String sessionId) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            return Optional.empty();
        }
        if (session.getUserId() != null && !managerFactory.userManager().getUser(session.getUserId()).isPresent()) {
            // can happen if user from same host signs in multiple time with multiple clients, after they disconnect with one client
            disconnect(sessionId, DisconnectReason.ConnectingOtherInstance, session); // direct disconnect
            return Optional.empty();
        }
        return Optional.of(session);
    }

    @Override
    public void createSession(String sessionId, InvokerCallbackHandler callbackHandler) {
        Session session = new Session(managerFactory, sessionId, callbackHandler);
        sessions.put(sessionId, session);
    }

    @Override
    public boolean registerUser(String sessionId, String userName, String password, String email) throws MageException {
        Session session = sessions.get(sessionId);
        if (session == null) {
            return false;
        }
        String returnMessage = session.registerUser(userName, password, email);
        if (returnMessage != null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean connectUser(String sessionId, String userName, String password, String userIdStr) throws MageException {
        Session session = sessions.get(sessionId);
        if (session != null) {
            String returnMessage = session.connectUser(userName, password);
            if (returnMessage == null) {
                return true;
            } else {
            }
        } else {
        }
        return false;
    }

    @Override
    public boolean connectAdmin(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session != null) {
            session.connectAdmin();
            return true;
        }
        return false;
    }

    @Override
    public boolean setUserData(String userName, String sessionId, UserData userData, String clientVersion, String userIdStr) throws MageException {
        return getSession(sessionId)
                .map(session -> session.setUserData(userName, userData, clientVersion, userIdStr))
                .orElse(false);

    }

    @Override
    public void disconnect(String sessionId, DisconnectReason reason) {
        disconnect(sessionId, reason, null);
    }

    @Override
    public void disconnect(String sessionId, DisconnectReason reason, Session directSession) {
        if (directSession == null) {
            // find real session to disconnects
            getSession(sessionId).ifPresent(session -> {
                if (!isValidSession(sessionId)) {
                    // session was removed meanwhile by another thread so we can return
                    return;
                }
                sessions.remove(sessionId);
                switch (reason) {
                    case AdminDisconnect:
                        session.kill(reason);
                        break;
                    case ConnectingOtherInstance:
                    case Disconnected: // regular session end or wrong client version
                        managerFactory.userManager().disconnect(session.getUserId(), reason);
                        break;
                    case SessionExpired: // session ends after no reconnect happens in the defined time span
                        break;
                    case LostConnection: // user lost connection - session expires countdown starts
                        session.userLostConnection();
                        managerFactory.userManager().disconnect(session.getUserId(), reason);
                        break;
                    default:
                }
            });
        } else {
            // direct session to disconnects
            sessions.remove(sessionId);
            directSession.kill(reason);
        }
    }


    /**
     * Admin requested the disconnect of a user
     *
     * @param sessionId
     * @param userSessionId
     */
    @Override
    public void disconnectUser(String sessionId, String userSessionId) {
        if (isAdmin(sessionId)) {
            getUserFromSession(sessionId).ifPresent(admin -> {
                Optional<User> u = getUserFromSession(userSessionId);
                if (u.isPresent()) {
                    User user = u.get();
                    user.showUserMessage("Admin operation", "Your session was disconnected by Admin.");
                    admin.showUserMessage("Admin action", "User" + user.getName() + " was disconnected.");
                    disconnect(userSessionId, DisconnectReason.AdminDisconnect);
                } else {
                    admin.showUserMessage("Admin operation", "User with sessionId " + userSessionId + " could not be found!");
                }
            });
        }
    }

    private Optional<User> getUserFromSession(String sessionId) {
        return getSession(sessionId)
                .flatMap(s -> managerFactory.userManager().getUser(s.getUserId()));

    }

    @Override
    public void endUserSession(String sessionId, String userSessionId) {
        if (isAdmin(sessionId)) {
            disconnect(userSessionId, DisconnectReason.AdminDisconnect);
        }
    }

    @Override
    public boolean isAdmin(String sessionId) {
        return getSession(sessionId).map(Session::isAdmin).orElse(false);

    }

    @Override
    public boolean isValidSession(@Nonnull String sessionId) {
        return sessions.containsKey(sessionId);
    }

    @Override
    public Optional<User> getUser(@Nonnull String sessionId) {
        Session session = sessions.get(sessionId);
        if (session != null) {
            return managerFactory.userManager().getUser(sessions.get(sessionId).getUserId());
        }
        return Optional.empty();
    }

    @Override
    public boolean extendUserSession(String sessionId, String pingInfo) {
        return getSession(sessionId)
                .map(session -> managerFactory.userManager().extendUserSession(session.getUserId(), pingInfo))
                .orElse(false);
    }

    @Override
    public void sendErrorMessageToClient(String sessionId, String message) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            return;
        }
        session.sendErrorMessageToClient(message);
    }
}
