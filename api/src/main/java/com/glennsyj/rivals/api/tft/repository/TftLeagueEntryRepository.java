package com.glennsyj.rivals.api.tft.repository;

import com.glennsyj.rivals.api.tft.entity.TftLeagueEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TftLeagueEntryRepository extends JpaRepository<TftLeagueEntry, Long> {

    /**
     * RiotAccount의 puuid로 TFT 리그 정보 조회
     * 일반 TFT 랭크 정보만 조회 (초고속 제외)
     */
    Optional<TftLeagueEntry> findByPuuid(String puuid);

    /**
     * RiotAccount의 id로 TFT 리그 정보 조회
     * 일반 TFT 랭크 정보만 조회 (초고속 제외)
     */
    Optional<TftLeagueEntry> findByAccount_Id(Long account_id);

    List<TftLeagueEntry> findAllByAccount_IdIn(List<Long> accountIds);

    Optional<TftLeagueEntry> findFirstByPuuidOrderByUpdatedAtDesc(String puuid);

    Optional<TftLeagueEntry> findFirstByAccount_IdOrderByUpdatedAtDesc(Long accountId);

    @Query("""
        SELECT e1 FROM TftLeagueEntry e1
        WHERE e1.account.id = :accountId
        AND e1.updatedAt = (
            SELECT MAX(e2.updatedAt)
            FROM TftLeagueEntry e2
            WHERE e2.account.id = e1.account.id
            AND e2.queueType = e1.queueType
        )
    """)
    List<TftLeagueEntry> findLatestEntriesForEachQueueTypeByAccountId(@Param("accountId") Long accountId);
}
