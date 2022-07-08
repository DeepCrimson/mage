package mage.server.managers;

import mage.server.draft.DraftController;

import java.util.Optional;
import java.util.UUID;

public interface DraftManager {

    void joinDraft(UUID draftId, UUID userId);

    void destroyChatSession(UUID gameId);

    void sendCardMark(UUID draftId, UUID userId, UUID cardId);

    void removeSession(UUID userId);

    void kill(UUID draftId, UUID userId);

    void timeout(UUID gameId, UUID userId);

    void removeDraft(UUID draftId);

    DraftController getControllerByDraftId(UUID draftId);

    Optional<DraftController> getController(UUID tableId);
}
