package mage.server;

import mage.cards.repository.CardInfo;
import mage.cards.repository.CardRepository;
import mage.game.Game;
import mage.server.exceptions.UserNotFoundException;
import mage.server.game.GameController;
import mage.server.managers.ChatManager;
import mage.server.managers.ManagerFactory;
import mage.server.util.SystemUtil;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import mage.game.Game;
import mage.util.CardUtil;
import mage.view.ChatMessage;

import java.io.Serializable;
import java.util.Date;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class ChatManagerImpl implements ChatManager {
    private static final HashMap<String, String> userMessages = new HashMap<>();
    private static final String COMMANDS_LIST
            = "<br/>List of commands:"
            + "<br/>\\history or \\h [username] - shows the history of a player"
            + "<br/>\\me - shows the history of the current player"
            + "<br/>\\list or \\l - Show a list of commands"
            + "<br/>\\whisper or \\w [player name] [text] - whisper to the player with the given name"
            + "<br/>\\card Card Name - Print oracle text for card"
            + "<br/>[Card Name] - Show a highlighted card name"
            + "<br/>\\ignore - shows your ignore list on this server."
            + "<br/>\\ignore [username] - add username to ignore list (they won't be able to chat or join to your game)."
            + "<br/>\\unignore [username] - remove a username from your ignore list on this server.";
    final Pattern cardNamePattern = Pattern.compile("\\[(.*?)\\]");
    final Pattern getCardTextPattern = Pattern.compile("^.card *(.*)");
    private final ManagerFactory managerFactory;
    private final ConcurrentHashMap<UUID, ChatSession> chatSessions = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public ChatManagerImpl(ManagerFactory managerFactory) {
        this.managerFactory = managerFactory;
    }

    @Override
    public UUID createChatSession(String info) {
        ChatSession chatSession = new ChatSession(managerFactory, info);
        chatSessions.put(chatSession.getChatId(), chatSession);
        return chatSession.getChatId();
    }

    @Override
    public void joinChat(UUID chatId, UUID userId) {
        ChatSession chatSession = chatSessions.get(chatId);
        if (chatSession != null) {
            chatSession.join(userId);
        } else {
        }

    }

    @Override
    public void clearUserMessageStorage() {
        userMessages.clear();
    }

    @Override
    public void leaveChat(UUID chatId, UUID userId) {
        ChatSession chatSession = chatSessions.get(chatId);
        if (chatSession != null && chatSession.hasUser(userId)) {
            chatSession.kill(userId, DisconnectReason.CleaningUp);
        }
    }

    @Override
    public void destroyChatSession(UUID chatId) {
        if (chatId != null) {
            ChatSession chatSession = chatSessions.get(chatId);
            if (chatSession != null) {
                if (chatSessions.containsKey(chatId)) {
                    final Lock w = lock.writeLock();
                    w.lock();
                    try {
                        chatSessions.remove(chatId);
                    } finally {
                        w.unlock();
                    }
                }
            }
        }
    }

    @Override
    public void broadcast(UUID chatId, String userName, String message, ChatMessage.MessageColor color, boolean withTime, Game game, ChatMessage.MessageType messageType, ChatMessage.SoundToPlay soundToPlay) {
    }

    private boolean performUserCommand(User user, String message, UUID chatId, boolean doError) {
        String command = message.substring(1).trim().toUpperCase(Locale.ENGLISH);
        if (doError) {
            message += new StringBuilder("<br/>Invalid User Command '" + message + "'.").append(COMMANDS_LIST).toString();
            message += "<br/>Type <font color=green>\\w " + user.getName() + " profanity 0 (or 1 or 2)</font> to use/not use the profanity filter";
            chatSessions.get(chatId).broadcastInfoToUser(user, message);
            return true;
        }

        if (command.startsWith("H ") || command.startsWith("HISTORY ")) {
            message += "<br/>" + managerFactory.userManager().getUserHistory(message.substring(command.startsWith("H ") ? 3 : 9));
            chatSessions.get(chatId).broadcastInfoToUser(user, message);
            return true;
        }
        if (command.equals("ME")) {
            message += "<br/>" + managerFactory.userManager().getUserHistory(user.getName());
            chatSessions.get(chatId).broadcastInfoToUser(user, message);
            return true;
        }
        if (command.startsWith("GAME")) {
            message += "<br/>";
            ChatSession session = chatSessions.get(chatId);
            if (session != null && session.getInfo() != null) {
                String gameId = session.getInfo();
                if (gameId.startsWith("Game ")) {
                    UUID id = java.util.UUID.fromString(gameId.substring(5));
                    for (Entry<UUID, GameController> entry : managerFactory.gameManager().getGameController().entrySet()) {
                        if (entry.getKey().equals(id)) {
                            GameController controller = entry.getValue();
                            if (controller != null) {
                                message += controller.getGameStateDebugMessage();
                                chatSessions.get(chatId).broadcastInfoToUser(user, message);
                            }
                        }
                    }

                }
            }
            return true;
        }
        if (command.startsWith("FIX")) {
            message += "<br/>";
            ChatSession session = chatSessions.get(chatId);
            if (session != null && session.getInfo() != null) {
                String gameId = session.getInfo();
                if (gameId.startsWith("Game ")) {
                    UUID id = java.util.UUID.fromString(gameId.substring(5));
                    for (Entry<UUID, GameController> entry : managerFactory.gameManager().getGameController().entrySet()) {
                        if (entry.getKey().equals(id)) {
                            GameController controller = entry.getValue();
                            if (controller != null) {
                                message += controller.attemptToFixGame(user);
                                chatSessions.get(chatId).broadcastInfoToUser(user, message);
                            }
                        }
                    }

                }
            }
            return true;
        }
        if (command.equals("PINGS")) {
            message += "<br/>";
            ChatSession session = chatSessions.get(chatId);
            if (session != null && session.getInfo() != null) {
                String gameId = session.getInfo();
                if (gameId.startsWith("Game ")) {
                    UUID id = java.util.UUID.fromString(gameId.substring(5));
                    for (Entry<UUID, GameController> entry : managerFactory.gameManager().getGameController().entrySet()) {
                        if (entry.getKey().equals(id)) {
                            GameController controller = entry.getValue();
                            if (controller != null) {
                                message += controller.getPingsInfo();
                                chatSessions.get(chatId).broadcastInfoToUser(user, message);
                            }
                        }
                    }

                }
            }
            return true;
        }
        if (command.startsWith("CARD ")) {
            Matcher matchPattern = getCardTextPattern.matcher(message.toLowerCase(Locale.ENGLISH));
            if (matchPattern.find()) {
                String cardName = matchPattern.group(1);
                CardInfo cardInfo = CardRepository.instance.findPreferredCoreExpansionCard(cardName, true);
                if (cardInfo != null) {
                    cardInfo.getRules();
                    message = "<font color=orange>" + cardInfo.getName() + "</font>: Cost:" + cardInfo.getManaCosts(CardInfo.ManaCostSide.ALL).toString() + ",  Types:" + cardInfo.getTypes().toString() + ", ";
                    for (String rule : cardInfo.getRules()) {
                        message = message + rule;
                    }
                } else {
                    message = "Couldn't find: " + cardName;

                }
            }
            chatSessions.get(chatId).broadcastInfoToUser(user, message);
            return true;
        }
        if (command.startsWith("W ") || command.startsWith("WHISPER ")) {
            String rest = message.substring(command.startsWith("W ") ? 3 : 9);
            int first = rest.indexOf(' ');
            if (first > 1) {
                String userToName = rest.substring(0, first);
                rest = rest.substring(first + 1).trim();
                Optional<User> userTo = managerFactory.userManager().getUserByName(userToName);
                if (userTo.isPresent()) {
                    if (!chatSessions.get(chatId).broadcastWhisperToUser(user, userTo.get(), rest)) {
                        message += new StringBuilder("<br/>User ").append(userToName).append(" not found").toString();
                        chatSessions.get(chatId).broadcastInfoToUser(user, message);
                    }
                } else {
                    message += new StringBuilder("<br/>User ").append(userToName).append(" not found").toString();
                    chatSessions.get(chatId).broadcastInfoToUser(user, message);
                }
                return true;
            }
        }
        if (command.equals("L") || command.equals("LIST")) {
            message += COMMANDS_LIST;
            message += "<br/>Type <font color=green>\\w " + user.getName() + " profanity 0 (or 1 or 2)</font> to use/not use the profanity filter";
            chatSessions.get(chatId).broadcastInfoToUser(user, message);
            return true;
        }
        return false;
    }

    @Override
    public void sendReconnectMessage(UUID userId) {
    }

    @Override
    public void sendLostConnectionMessage(UUID userId, DisconnectReason reason) {
    }

    /**
     * Send message to all active waiting/tourney/game chats (but not in main lobby)
     *
     * @param userId
     * @param message
     */
    @Override
    public void sendMessageToUserChats(UUID userId, String message) {
        managerFactory.userManager().getUser(userId).ifPresent(user -> {
            List<ChatSession> chatSessions = getChatSessions().stream()
                    .filter(chat -> !chat.getChatId().equals(managerFactory.gamesRoomManager().getMainChatId())) // ignore main lobby
                    .filter(chat -> chat.hasUser(userId))
                    .collect(Collectors.toList());
        });
    }

    @Override
    public void removeUser(UUID userId, DisconnectReason reason) {
        for (ChatSession chatSession : getChatSessions()) {
            if (chatSession.hasUser(userId)) {
                chatSession.kill(userId, reason);
            }
        }
    }

    @Override
    public List<ChatSession> getChatSessions() {
        final Lock r = lock.readLock();
        r.lock();
        try {
            return new ArrayList<>(chatSessions.values());
        } finally {
            r.unlock();
        }
    }

}
