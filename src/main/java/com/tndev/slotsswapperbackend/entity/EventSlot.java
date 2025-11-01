package com.tndev.slotsswapperbackend.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
@Document("events")
@Data
public class EventSlot {
    @Id
    private String id;
    private String title;
    private Instant startTime;
    private Instant endTime;
    private SlotStatus status=SlotStatus.BUSY;
    private String ownerId;
    private String ownerName;
}


