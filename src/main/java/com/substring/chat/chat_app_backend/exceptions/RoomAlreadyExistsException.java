package com.substring.chat.chat_app_backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when attempting to create a room with an ID that already exists.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class RoomAlreadyExistsException extends RuntimeException {

    private final String roomId;

    public RoomAlreadyExistsException(String roomId) {
        super("Room already exists: " + roomId);
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }
}
