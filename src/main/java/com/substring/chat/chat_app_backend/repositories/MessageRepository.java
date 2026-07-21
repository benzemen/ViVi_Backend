package com.substring.chat.chat_app_backend.repositories;

import com.substring.chat.chat_app_backend.entities.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * MongoDB repository for {@link Message} documents.
 *
 * <p>Provides database-level pagination and querying for messages,
 * replacing the previous in-memory subList approach that loaded
 * all messages into memory.</p>
 */
public interface MessageRepository extends MongoRepository<Message, String> {

    /**
     * Retrieves messages for a given room, ordered by timestamp (newest first),
     * with database-level pagination.
     *
     * @param roomId   the room identifier
     * @param pageable pagination parameters (page number, page size)
     * @return a page of messages
     */
    Page<Message> findByRoomIdOrderByTimeStampDesc(String roomId, Pageable pageable);

    /**
     * Retrieves messages for a room, excluding those hidden by the given user.
     *
     * @param roomId   the room identifier
     * @param username the username to exclude hidden messages for
     * @param pageable pagination parameters
     * @return a page of messages
     */
    Page<Message> findByRoomIdAndHiddenByNotContainingOrderByTimeStampDesc(String roomId, String username, Pageable pageable);

    /**
     * Counts the total number of messages in a room.
     *
     * @param roomId the room identifier
     * @return the message count
     */
    long countByRoomId(String roomId);

    /**
     * Deletes all messages in a given room.
     *
     * @param roomId the room identifier
     */
    void deleteByRoomId(String roomId);
}
