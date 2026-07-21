package com.substring.chat.chat_app_backend.services;

import com.substring.chat.chat_app_backend.entities.Message;
import com.substring.chat.chat_app_backend.exceptions.ContentModerationException;
import com.substring.chat.chat_app_backend.repositories.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service layer for message operations.
 *
 * <p>
 * Handles message creation (with optional moderation), persistence,
 * and database-level paginated retrieval. This replaces the previous
 * approach of embedding messages inside Room documents and paginating
 * via in-memory {@code subList()}.
 * </p>
 */
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final RoomService roomService;
    private final ModerationService moderationService;

    public MessageService(MessageRepository messageRepository,
            RoomService roomService,
            ModerationService moderationService) {
        this.messageRepository = messageRepository;
        this.roomService = roomService;
        this.moderationService = moderationService;
    }

    /**
     * Creates and persists a new message in the given room.
     *
     * Validates that the room exists, runs the content through the moderation
     * service (if enabled), then persists and returns the message.
     * </p>
     *
     * @param roomId  the target room identifier
     * @param sender  the username of the message sender
     * @param content the message body text
     * @return the saved Message entity
     * @throws com.substring.chat.chat_app_backend.exceptions.RoomNotFoundException if
     *                                                                              the
     *                                                                              room
     *                                                                              doesn't
     *                                                                              exist
     * @throws ContentModerationException                                           if
     *                                                                              the
     *                                                                              content
     *                                                                              is
     *                                                                              flagged
     *                                                                              by
     *                                                                              moderation
     */
    public Message sendMessage(String roomId, String sender, String content) {
        // Verify room exists (throws RoomNotFoundException if not)
        roomService.getRoomByRoomId(roomId);

        // Run moderation check
        if (!moderationService.isContentAllowed(content)) {
            throw new ContentModerationException("Content flagged as inappropriate");
        }

        // Filter content (e.g., mask profanity) and persist
        String filteredContent = moderationService.filterContent(content);
        Message message = new Message(sender, filteredContent, roomId);
        return messageRepository.save(message);
    }

    /**
     * Retrieves paginated messages for a room, ordered by timestamp (newest first).
     *
     * This uses MongoDB's native skip/limit pagination instead of loading
     * all messages into memory — critical for rooms with thousands of messages.
     * </p>
     *
     * @param roomId   the room identifier
     * @param page     zero-based page number
     * @param size     number of messages per page
     * @param username the username to exclude hidden messages for (nullable)
     * @return a page of messages
     */
    public Page<Message> getMessages(String roomId, int page, int size, String username) {
        // Verify room exists
        roomService.getRoomByRoomId(roomId);

        Pageable pageable = PageRequest.of(page, size);
        if (username != null) {
            return messageRepository.findByRoomIdAndHiddenByNotContainingOrderByTimeStampDesc(roomId, username, pageable);
        }
        return messageRepository.findByRoomIdOrderByTimeStampDesc(roomId, pageable);
    }

    /**
     * Hides a message for a specific user.
     *
     * @param messageId the message identifier
     * @param username  the username hiding the message
     */
    public void hideMessageForUser(String messageId, String username) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        if (message.getHiddenBy() == null) {
            message.setHiddenBy(new java.util.ArrayList<>());
        }
        if (!message.getHiddenBy().contains(username)) {
            message.getHiddenBy().add(username);
            messageRepository.save(message);
        }
    }

    /**
     * Deletes a specific message by its ID.
     *
     * @param messageId the message identifier
     */
    public void deleteMessage(String messageId) {
        messageRepository.deleteById(messageId);
    }

    /**
     * Deletes all messages in a given room.
     *
     * @param roomId the room identifier
     */
    public void deleteAllMessagesInRoom(String roomId) {
        messageRepository.deleteByRoomId(roomId);
    }
}
