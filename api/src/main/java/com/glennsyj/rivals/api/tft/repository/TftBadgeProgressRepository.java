package com.glennsyj.rivals.api.tft.repository;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.tft.entity.achievement.TftBadgeProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface TftBadgeProgressRepository extends JpaRepository<TftBadgeProgress, Long> {
    
    Optional<TftBadgeProgress> findByRiotAccountAndBadgeType(
        RiotAccount riotAccount,
        TftBadgeProgress.BadgeType badgeType
    );

    List<TftBadgeProgress> findByRiotAccountAndIsActiveTrue(RiotAccount riotAccount);

    List<TftBadgeProgress> findByRiotAccount(RiotAccount riotAccount);

    @Query("SELECT bp FROM TftBadgeProgress bp WHERE bp.riotAccount IN :accounts")
    List<TftBadgeProgress> findByRiotAccountIn(List<RiotAccount> accounts);

    // Return Type을 Map으로 이용하기 위해서 작성
    default Map<String, List<TftBadgeProgress>> findMapByRiotAccountIn(List<RiotAccount> accounts) {
        return findByRiotAccountIn(accounts).stream()
                .collect(Collectors.groupingBy(bp -> bp.getRiotAccount().getPuuid()));
    }
}