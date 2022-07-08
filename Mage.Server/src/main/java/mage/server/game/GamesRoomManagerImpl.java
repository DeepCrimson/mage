package mage.server.game;

import mage.server.managers.GamesRoomManager;
import mage.server.managers.ManagerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class GamesRoomManagerImpl implements GamesRoomManager {

    private final ManagerFactory managerFactory;
    private final ConcurrentHashMap<UUID, GamesRoom> rooms = new ConcurrentHashMap<>();
    private UUID mainRoomId;
    private UUID mainChatId;


    public GamesRoomManagerImpl(ManagerFactory managerFactory) {
        this.managerFactory = managerFactory;
    }

    public void init() {
        GamesRoom mainRoom = new GamesRoomImpl(managerFactory);
        mainRoomId = mainRoom.getRoomId();
        mainChatId = mainRoom.getChatId();
        rooms.put(mainRoomId, mainRoom);
    }

    @Override
    public UUID createRoom() {
        GamesRoom room = new GamesRoomImpl(managerFactory);
        rooms.put(room.getRoomId(), room);
        return room.getRoomId();
    }

    @Override
    public UUID getMainRoomId() {
        return mainRoomId;
    }

    @Override
    public UUID getMainChatId() {
        return mainChatId;
    }

    @Override
    public Optional<GamesRoom> getRoom(UUID roomId) {
        if (rooms.containsKey(roomId)) {
            return Optional.of(rooms.get(roomId));
        }
        return Optional.empty();

    }

    @Override
    public void removeTable(UUID tableId) {
        for (GamesRoom room : rooms.values()) {
            room.removeTable(tableId);
        }
    }

}
