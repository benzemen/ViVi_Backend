package com.substring.chat.chat_app_backend.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Represents a chat room persisted in MongoDB.
 *
 * <p>
 * Each room has a unique {@code roomId} (user-facing identifier) separate from
 * the MongoDB-generated {@code id}. Messages are stored in a separate
 * {@link Message} collection linked by {@code roomId}, not embedded here.
 * </p>
 *
 * @see Message
 */
@Document(collection = "rooms")
@Getter
@Setter
@NoArgsConstructor
public class Room {

    /** MongoDB-generated unique identifier. */
    @Id
    private String id;

    /**
     * User-facing room identifier (e.g., "general", "java-help"). Must be unique.
     */
    @Indexed(unique = true)
    private String roomId;

    /** Timestamp when the room was created. */
    private LocalDateTime createdAt;

    /**
     * Constructs a room with the given user-facing room ID.
     * The creation timestamp is automatically set to the current time.
     *
     * @param roomId the unique, user-facing identifier for this room
     */
    public Room(String roomId) {
        this.roomId = roomId;
        this.createdAt = LocalDateTime.now();
    }
}
