package com.tndev.slotsswapperbackend.controllers;

import com.tndev.slotsswapperbackend.entity.EventSlot;
import com.tndev.slotsswapperbackend.entity.SlotStatus;
import com.tndev.slotsswapperbackend.repositories.EventRepository;
import com.tndev.slotsswapperbackend.services.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "slotsswapper.netlify.app")
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository eventRepository;
    private final JwtService jwtService;

    public EventController(EventRepository eventRepository, JwtService jwtService) {
        this.eventRepository = eventRepository;
        this.jwtService = jwtService;
    }

    // 🟢 Extract userId from JWT in the Authorization header
    private String extractUserIdFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        // FIX 5: Use the new extractUserId which reads from the 'userId' claim
        return jwtService.extractUserId(token);
    }

    // 🟩 Create a new event
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody EventSlot event) {
        String userId = extractUserIdFromHeader(authHeader);
        event.setOwnerId(userId);
        event.setStatus(SlotStatus.BUSY);
        EventSlot saved = eventRepository.save(event);
        return ResponseEntity.ok(saved);
    }

    // 🟨 Get all events for the logged-in user
    @GetMapping("/my")
    public ResponseEntity<?> getMyEvents(@RequestHeader("Authorization") String authHeader) {
        String userId = extractUserIdFromHeader(authHeader);
        List<EventSlot> events = eventRepository.findByOwnerId(userId);
        return ResponseEntity.ok(events);
    }

    // 🟧 Update event status (BUSY / SWAPPABLE)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@RequestHeader("Authorization") String authHeader,
                                          @PathVariable String id,
                                          @RequestBody Map<String, String> body) {
        String userId = extractUserIdFromHeader(authHeader);
        EventSlot event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getOwnerId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("message", "Unauthorized"));
        }

        String newStatus = body.get("status");
        event.setStatus(SlotStatus.valueOf(newStatus));
        eventRepository.save(event);

        return ResponseEntity.ok(Map.of("message", "Status updated", "event", event));
    }

    // 🟦 Get all swappable slots from other users
    @GetMapping("/swappable")
    public ResponseEntity<?> getSwappableSlots(@RequestHeader("Authorization") String authHeader) {
        String userId = extractUserIdFromHeader(authHeader);
        List<EventSlot> swappable = eventRepository.findByStatusAndOwnerIdNot(SlotStatus.SWAPPABLE, userId);
        return ResponseEntity.ok(swappable);
    }

    // 🟥 Delete event
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@RequestHeader("Authorization") String authHeader,
                                         @PathVariable String id) {
        String userId = extractUserIdFromHeader(authHeader);
        EventSlot event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getOwnerId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("message", "Unauthorized"));
        }

        eventRepository.delete(event);
        return ResponseEntity.ok(Map.of("message", "Event deleted"));
    }
}