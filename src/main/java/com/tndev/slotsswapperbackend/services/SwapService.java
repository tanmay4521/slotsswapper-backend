package com.tndev.slotsswapperbackend.services;

import com.tndev.slotsswapperbackend.entity.*;
import com.tndev.slotsswapperbackend.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SwapService {

    private final EventRepository eventRepo;
    private final SwapRequestRepository swapRepo;
    private final UserRepository userRepo;

    public SwapService(EventRepository eventRepo, SwapRequestRepository swapRepo, UserRepository userRepo) {
        this.eventRepo = eventRepo;
        this.swapRepo = swapRepo;
        this.userRepo = userRepo;
    }

    /**
     * Create a new swap request between two swappable event slots.
     */
    @Transactional
    public SwapRequest createRequest(String requesterId, String mySlotId, String theirSlotId) {
        EventSlot my = eventRepo.findById(mySlotId)
                .orElseThrow(() -> new IllegalArgumentException("My slot not found"));
        EventSlot their = eventRepo.findById(theirSlotId)
                .orElseThrow(() -> new IllegalArgumentException("Their slot not found"));

        if (!my.getOwnerId().equals(requesterId)) {
            throw new IllegalArgumentException("You do not own this slot.");
        }

        if (my.getStatus() != SlotStatus.SWAPPABLE || their.getStatus() != SlotStatus.SWAPPABLE) {
            throw new IllegalStateException("Both slots must be in SWAPPABLE state.");
        }

        User requester = userRepo.findById(requesterId).orElse(null);
        User responder = userRepo.findById(their.getOwnerId()).orElse(null);

        SwapRequest req = SwapRequest.builder()
                .requesterId(requesterId)
                .responderId(their.getOwnerId())
                .mySlotId(mySlotId)
                .theirSlotId(theirSlotId)
                .status(SwapStatus.PENDING)
                .build();

        if (requester != null) req.setRequesterName(requester.getName());
        if (responder != null) req.setResponderName(responder.getName());

        swapRepo.save(req);

        my.setStatus(SlotStatus.SWAP_PENDING);
        their.setStatus(SlotStatus.SWAP_PENDING);
        eventRepo.save(my);
        eventRepo.save(their);

        return req;
    }

    /**
     * Respond to a swap request — either accept or reject it.
     */
    @Transactional
    public void respond(String responderId, String requestId, boolean accept) {
        SwapRequest req = swapRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Swap request not found."));

        if (!req.getResponderId().equals(responderId)) {
            throw new SecurityException("You are not authorized to respond to this request.");
        }

        EventSlot my = eventRepo.findById(req.getMySlotId())
                .orElseThrow(() -> new IllegalArgumentException("My slot not found"));
        EventSlot their = eventRepo.findById(req.getTheirSlotId())
                .orElseThrow(() -> new IllegalArgumentException("Their slot not found"));

        if (!accept) {
            req.setStatus(SwapStatus.REJECTED);
            swapRepo.save(req);

            my.setStatus(SlotStatus.SWAPPABLE);
            their.setStatus(SlotStatus.SWAPPABLE);
            eventRepo.save(my);
            eventRepo.save(their);

            return;
        }

        // Accept: swap owners and set statuses to BUSY
        String ownerA = my.getOwnerId();
        String ownerB = their.getOwnerId();

        my.setOwnerId(ownerB);
        their.setOwnerId(ownerA);

        Optional<User> userA = userRepo.findById(ownerA);
        Optional<User> userB = userRepo.findById(ownerB);
        userA.ifPresent(u -> their.setOwnerName(u.getName()));
        userB.ifPresent(u -> my.setOwnerName(u.getName()));

        my.setStatus(SlotStatus.BUSY);
        their.setStatus(SlotStatus.BUSY);

        req.setStatus(SwapStatus.ACCEPTED);
        swapRepo.save(req);

        eventRepo.save(my);
        eventRepo.save(their);
    }
}
