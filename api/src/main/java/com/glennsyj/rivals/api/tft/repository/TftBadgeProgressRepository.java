package com.glennsyj.rivals.api.tft.repository;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.tft.entity.achievement.TftBadgeProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TftBadgeProgressRepository extends JpaRepository<TftBadgeProgress, Long> {
    
    Optional<TftBadgeProgress> findByRiotAccountAndBadgeType(
        RiotAccount riotAccount,
        TftBadgeProgress.BadgeType badgeType
    );

    List<TftBadgeProgress> findByRiotAccountAndIsActiveTrue(RiotAccount riotAccount);
} 