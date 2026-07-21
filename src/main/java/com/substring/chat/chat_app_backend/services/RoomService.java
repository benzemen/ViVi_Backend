package com.substring.chat.chat_app_backend.services;

import com.substring.chat.chat_app_backend.entities.Room;
import com.substring.chat.chat_app_backend.exceptions.RoomAlreadyExistsException;
import com.substring.chat.chat_app_backend.exceptions.RoomNotFoundException;
import com.substring.chat.chat_app_backend.repositories.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for room management operations.
 *
 * <p>Encapsulates business logic for creating, finding, and listing chat rooms,
 * keeping controllers thin and enabling reuse across REST and WebSocket handlers.</p>
 */
@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Creates a new chat room with the given room ID.
     *
     * @param roomId the unique, user-facing identifier for the room
     * @return the saved Room entity
     * @throws RoomAlreadyExistsException if a room with this ID already exists
     */
    public Room createRoom(String roomId) {
        if (roomRepository.existsByRoomId(roomId)) {
            throw new RoomAlreadyExistsException(roomId);
        }
        return roomRepository.save(new Room(roomId));
    }

    /**
     * Retrieves a room by its user-facing room ID.
     *
     * @param roomId the room identifier
     * @return the Room entity
     * @throws RoomNotFoundException if no room with this ID exists
     */
    public Room getRoomByRoomId(String roomId) {
        Room room = roomRepository.findByRoomId(roomId);
        if (room == null) {
            throw new RoomNotFoundException(roomId);
        }
        return room;
    }

    /**
     * Returns all existing rooms.
     *
     * @return list of all rooms
     */
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    /**
     * Deletes a room by its user-facing room ID.
     *
     * @param roomId the room identifier
     */
    public void deleteRoom(String roomId) {
        Room room = getRoomByRoomId(roomId);
        roomRepository.delete(room);
    }
}
