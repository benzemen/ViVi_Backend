package com.substring.chat.chat_app_backend.controllers;

import com.substring.chat.chat_app_backend.entities.Message;
import com.substring.chat.chat_app_backend.entities.Room;
import com.substring.chat.chat_app_backend.services.MessageService;
import com.substring.chat.chat_app_backend.services.RoomService;
import com.substring.chat.chat_app_backend.services.SummarizationService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for room management and message retrieval.
 *
 * <p>
 * Provides endpoints for creating rooms, joining rooms, retrieving
 * paginated message history, and generating conversation summaries.
 * </p>
 *
 * <h3>Endpoints</h3>
 * <ul>
 * <li>{@code POST /api/v1/rooms} — Create a new room</li>
 * <li>{@code GET /api/v1/rooms/{roomId}} — Join / get room details</li>
 * <li>{@code GET /api/v1/rooms} — List all rooms</li>
 * <li>{@code GET /api/v1/rooms/{roomId}/messages?page=0&size=20} — Paginated
 * messages</li>
 * <li>{@code GET /api/v1/rooms/{roomId}/summary?count=50} — AI-generated
 * summary</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;
    private final MessageService messageService;
    private final SummarizationService summarizationService;

    public RoomController(RoomService roomService,
            MessageService messageService,
            SummarizationService summarizationService) {
        this.roomService = roomService;
        this.messageService = messageService;
        this.summarizationService = summarizationService;
    }

    /**
     * Creates a new chat room.
     *
     * @param request the room details (must contain a non-null {@code roomId})
     * @return 201 Created with the saved room, or 409 Conflict if it already exists
     */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Room> createRoom(@RequestBody Room request) {
        Room saved = roomService.createRoom(request.getRoomId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Retrieves a room by its user-facing room ID. Used for "joining" a room.
     *
     * @param roomId the room identifier
     * @return 200 OK with the room details, or 404 if not found
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<Room> joinRoom(@PathVariable @NotBlank String roomId) {
        Room room = roomService.getRoomByRoomId(roomId);
        return ResponseEntity.ok(room);
    }

    /**
     * Lists all available rooms.
     *
     * @return 200 OK with the list of all rooms
     */
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    /**
     * Retrieves paginated messages for a room, ordered by timestamp (newest first).
     *
     * <p>
     * Pagination is performed at the database level using MongoDB's skip/limit,
     * not in application memory.
     * </p>
     *
     * @param roomId the room identifier
     * @param page   zero-based page number (default: 0)
     * @param size   messages per page (default: 20)
     * @return 200 OK with a page of messages
     */
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<Page<Message>> getMessages(
            @PathVariable @NotBlank String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            java.security.Principal principal) {

        String username = principal != null ? principal.getName() : null;
        Page<Message> messages = messageService.getMessages(roomId, page, size, username);
        return ResponseEntity.ok(messages);
    }

    /**
     * Generates an AI-powered summary of recent messages in a room.
     *
     * <p>
     * This "catch me up" feature is useful when a user joins a room with a
     * long message history. Requires {@code app.summarization.enabled=true}
     * and a configured AI API key.
     * </p>
     *
     * @param roomId the room identifier
     * @param count  number of recent messages to summarize (default: 50)
     * @return 200 OK with the summary text
     */
    @GetMapping("/{roomId}/summary")
    public ResponseEntity<Map<String, String>> getSummary(
            @PathVariable @NotBlank String roomId,
            @RequestParam(defaultValue = "50") int count,
            java.security.Principal principal) {

        // Verify room exists (throws RoomNotFoundException if not)
        roomService.getRoomByRoomId(roomId);

        // Pass username so messages hidden/deleted by this user are excluded
        String username = principal != null ? principal.getName() : null;
        String summary = summarizationService.summarizeRecentMessages(roomId, count, username);
        return ResponseEntity.ok(Map.of("summary", summary));
    }

    /**
     * Deletes a chat room and all of its associated messages.
     *
     * @param roomId the room identifier
     * @return 204 No Content on success
     */
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable @NotBlank String roomId) {
        roomService.deleteRoom(roomId);
        messageService.deleteAllMessagesInRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a specific message in a chat room.
     *
     * @param roomId    the room identifier
     * @param messageId the message identifier
     * @return 204 No Content on success
     */
    @DeleteMapping("/{roomId}/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable @NotBlank String roomId,
            @PathVariable @NotBlank String messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Hides a specific message for the current user.
     *
     * @param roomId    the room identifier
     * @param messageId the message identifier
     * @param principal the authenticated user
     * @return 204 No Content on success
     */
    @PostMapping("/{roomId}/messages/{messageId}/hide")
    public ResponseEntity<Void> hideMessage(
            @PathVariable @NotBlank String roomId,
            @PathVariable @NotBlank String messageId,
            java.security.Principal principal) {
        if (principal != null) {
            messageService.hideMessageForUser(messageId, principal.getName());
        }
        return ResponseEntity.noContent().build();
    }
}
