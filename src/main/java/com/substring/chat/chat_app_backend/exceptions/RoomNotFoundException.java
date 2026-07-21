package com.substring.chat.chat_app_backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an operation references a room that does not exist in the database.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RoomNotFoundException extends RuntimeException {

    private final String roomId;

    public RoomNotFoundException(String roomId) {
        super("Room not found: " + roomId);
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }
}
