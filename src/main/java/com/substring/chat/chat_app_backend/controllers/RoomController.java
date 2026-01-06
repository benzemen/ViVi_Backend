
package com.substring.chat.chat_app_backend.controllers;

import com.substring.chat.chat_app_backend.entities.Message;
import com.substring.chat.chat_app_backend.entities.Room;
import com.substring.chat.chat_app_backend.repositories.RoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.substring.chat.chat_app_backend.configure.AppConstants;


@RestController
@RequestMapping("/api/v1/rooms")
@CrossOrigin(AppConstants.FRONT_END_BASE_URL)
public class RoomController {

    private final RoomRepository roomRepository;

// injection karna bolthe hai isse
// spring hame khud object banake deta hai isse injection khathe hai isse hota ye hai ki hame khud object banane ki jarurat nahi
    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }



    // create room
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody String roomId) {

        if (roomRepository.findByRoomId(roomId) != null) {
            return ResponseEntity.badRequest().body("Room already exists!");
        }

        Room room = new Room();
        room.setRoomId(roomId);

        Room savedRoom = roomRepository.save(room);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedRoom);
    }

    // join room
    @GetMapping("/{roomId}")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId) {

        Room room = roomRepository.findByRoomId(roomId);
        if (room == null) {
            return ResponseEntity.badRequest().body("Room not found!");
        }

        return ResponseEntity.ok(room);
    }

    // get messages from a particular room by giving its room id

    @GetMapping("/{roomId}/messages")

    public ResponseEntity<List<Message>> getMessages(
            @PathVariable String roomId,


// parsing  kara hai isme na aisa hota hai ki sare message ek sath bhejne nahi padthe kuch kuch page bhejthe hai jisme hum unh message ko dekthe hai

            @RequestParam(defaultValue = "0") int page, // isse na page start hohga
            @RequestParam(defaultValue = "20") int size) // aur isse page a size fix kar diya ki ek page mai 20 message hi aeinge
    {

        Room room = roomRepository.findByRoomId(roomId);
        if (room == null) {
            return ResponseEntity.badRequest().build();
        }

        List<Message> messages = room.getMessages();
        if (messages == null || messages.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        int start = Math.max(0, messages.size() - (page + 1) * size);
        int end = Math.min(start + size, messages.size());

        return ResponseEntity.ok(messages.subList(start, end));
    }
}
