package com.substring.chat.chat_app_backend.repositories;

import com.substring.chat.chat_app_backend.entities.Room;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * MongoDB repository for {@link Room} documents.
 */
public interface RoomRepository extends MongoRepository<Room, String> {

    /**
     * Finds a room by its user-facing room ID.
     *
     * @param roomId the unique room identifier (e.g., "general")
     * @return the room, or {@code null} if not found
     */
    Room findByRoomId(String roomId);

    /**
     * Checks whether a room with the given ID already exists.
     *
     * @param roomId the room identifier to check
     * @return {@code true} if a room with this ID exists
     */
    boolean existsByRoomId(String roomId);
}
