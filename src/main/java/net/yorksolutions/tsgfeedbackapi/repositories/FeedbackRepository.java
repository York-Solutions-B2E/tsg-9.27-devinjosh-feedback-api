package net.yorksolutions.tsgfeedbackapi.repositories;

import net.yorksolutions.tsgfeedbackapi.repositories.entities.FeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeedbackRepository extends JpaRepository<FeedbackEntity, UUID> {
    List<FeedbackEntity> findByMemberId(String memberId);
}

