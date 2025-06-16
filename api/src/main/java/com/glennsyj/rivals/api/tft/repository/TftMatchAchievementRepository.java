package com.glennsyj.rivals.api.tft.repository;

import com.glennsyj.rivals.api.tft.entity.achievement.AchievementType;
import com.glennsyj.rivals.api.tft.entity.achievement.TftMatchAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TftMatchAchievementRepository extends JpaRepository<TftMatchAchievement, Long> {
    
    @Query(nativeQuery = true, value = """
        WITH recent_matches AS (
            SELECT m.id
            FROM tft_matches m
            JOIN tft_match_participants p ON m.id = p.match_id
            WHERE p.puuid = :puuid
            ORDER BY m.game_creation DESC
            LIMIT :recentCount
        )
        SELECT COUNT(*)
        FROM tft_match_achievements a
        JOIN tft_match_participants p ON a.participant_id = p.id
        WHERE p.puuid = :puuid
        AND a.type = :achievementType
        AND a.match_id IN (SELECT id FROM recent_matches)
    """)
    int countRecentAchievements(
        String puuid,
        String achievementType,
        int recentCount
    );
} 