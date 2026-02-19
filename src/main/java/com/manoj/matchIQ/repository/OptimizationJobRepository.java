package com.manoj.matchIQ.repository;

import com.manoj.matchIQ.model.OptimizationJob;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OptimizationJobRepository extends MongoRepository<OptimizationJob, String> {
}
