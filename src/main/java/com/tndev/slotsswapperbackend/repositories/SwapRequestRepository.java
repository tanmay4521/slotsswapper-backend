package com.tndev.slotsswapperbackend.repositories;

import com.tndev.slotsswapperbackend.entity.SwapRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SwapRequestRepository extends MongoRepository<SwapRequest, String> {
    List<SwapRequest> findByResponderId(String responderId);
    List<SwapRequest> findByRequesterId(String requesterId);
}
