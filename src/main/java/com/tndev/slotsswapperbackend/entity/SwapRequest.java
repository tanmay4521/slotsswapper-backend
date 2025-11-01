package com.tndev.slotsswapperbackend.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("swap_request")
@Data
@Builder
public class SwapRequest {
    @Id
    private String id;
    private String requesterId;
    private String requesterName;
    private String responderName;
    private String responderId;
    private String mySlotId;
    private String theirSlotId;
    private SwapStatus status; // PENDING, ACCEPTED, REJECTED
    private Instant createdAt;


}

