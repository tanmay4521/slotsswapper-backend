package com.tndev.slotsswapperbackend.repositories;

import com.tndev.slotsswapperbackend.entity.EventSlot;
import com.tndev.slotsswapperbackend.entity.SlotStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EventRepository extends MongoRepository<EventSlot, String> {
    List<EventSlot> findByStatusAndOwnerIdNot(SlotStatus status, String ownerId);
    List<EventSlot> findByOwnerId(String ownerId);
}
