package com.tndev.slotsswapperbackend.controllers;

import com.tndev.slotsswapperbackend.entity.SlotStatus;
import com.tndev.slotsswapperbackend.entity.SwapRequest;
import com.tndev.slotsswapperbackend.repositories.EventRepository;
import com.tndev.slotsswapperbackend.repositories.SwapRequestRepository;
import com.tndev.slotsswapperbackend.services.JwtService;
import com.tndev.slotsswapperbackend.services.SwapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class SwapController {

    private final SwapService swapService;
    private final SwapRequestRepository swapRepo;
    private final EventRepository eventRepo;
    private final JwtService jwtService;

    public SwapController(SwapService swapService,
                          SwapRequestRepository swapRepo,
                          EventRepository eventRepo,
                          JwtService jwtService) {
        this.swapService = swapService;
        this.swapRepo = swapRepo;
        this.eventRepo = eventRepo;
        this.jwtService = jwtService;
    }

    private String getUserIdFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing Authorization header");
        }

        String token = authHeader.substring(7);
        if (!jwtService.isTokenValid(token)) {
            throw new RuntimeException("Invalid or expired token");
        }

        // FIX 6: Use the new extractUserId which reads from the 'userId' claim
        return jwtService.extractUserId(token);
    }

    @GetMapping("/swappable-slots")
    public ResponseEntity<?> getSwappableSlots(@RequestHeader("Authorization") String authHeader) {
        String userId = getUserIdFromHeader(authHeader);
        return ResponseEntity.ok(eventRepo.findByStatusAndOwnerIdNot(SlotStatus.SWAPPABLE, userId));
    }

    @PostMapping("/swap-request")
    public ResponseEntity<?> createSwapRequest(@RequestHeader("Authorization") String authHeader,
                                               @RequestBody Map<String, String> payload) {
        String userId = getUserIdFromHeader(authHeader);
        String mySlotId = payload.get("mySlotId");
        String theirSlotId = payload.get("theirSlotId");

        SwapRequest request = swapService.createRequest(userId, mySlotId, theirSlotId);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/swap-response/{requestId}")
    public ResponseEntity<?> respondToSwap(@RequestHeader("Authorization") String authHeader,
                                           @PathVariable String requestId,
                                           @RequestBody Map<String, Boolean> payload) {
        String userId = getUserIdFromHeader(authHeader);
        boolean accept = Boolean.TRUE.equals(payload.get("accept"));

        swapService.respond(userId, requestId, accept);
        return ResponseEntity.ok(Map.of("message", accept ? "Accepted" : "Rejected"));
    }

    @GetMapping("/swap-requests/incoming")
    public ResponseEntity<?> getIncomingRequests(@RequestHeader("Authorization") String authHeader) {
        String userId = getUserIdFromHeader(authHeader);
        List<SwapRequest> incoming = swapRepo.findByResponderId(userId);
        return ResponseEntity.ok(incoming);
    }

    @GetMapping("/swap-requests/outgoing")
    public ResponseEntity<?> getOutgoingRequests(@RequestHeader("Authorization") String authHeader) {
        String userId = getUserIdFromHeader(authHeader);
        List<SwapRequest> outgoing = swapRepo.findByRequesterId(userId);
        return ResponseEntity.ok(outgoing);
    }
}