package com.substring.chat.chat_app_backend.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Represents a single chat message persisted in MongoDB.
 *
 * <p>Each message belongs to a {@link Room} identified by {@code roomId}.
 * Messages are stored in their own collection ({@code "messages"}) rather than
 * embedded inside a Room document, avoiding MongoDB's 16 MB document size limit
 * and enabling efficient database-level pagination.</p>
 *
 * @see Room
 */
@Document(collection = "messages")
@Getter
@Setter
@NoArgsConstructor
public class Message {

    @Id
    private String id;

    /** The username of the message sender. */
    @Indexed
    private String sender;

    /** The message body text. */
    private String content;

    /** Foreign key linking this message to a {@link Room}. Indexed for fast lookups. */
    @Indexed
    private String roomId;

    /** Timestamp when the message was created. Indexed for chronological queries. */
    @Indexed
    private LocalDateTime timeStamp;

    /** List of usernames who have chosen to hide (delete for me) this message. */
    private java.util.List<String> hiddenBy = new java.util.ArrayList<>();

    /**
     * Constructs a message with the given sender, content, and room ID.
     * The timestamp is automatically set to the current time.
     *
     * @param sender  the username of the message sender
     * @param content the message body text
     * @param roomId  the ID of the room this message belongs to
     */
    public Message(String sender, String content, String roomId) {
        this.sender = sender;
        this.content = content;
        this.roomId = roomId;
        this.timeStamp = LocalDateTime.now();
    }
}
