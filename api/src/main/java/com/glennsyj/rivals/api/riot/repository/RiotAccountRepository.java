package com.glennsyj.rivals.api.riot.repository;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.rivalry.entity.Rivalry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiotAccountRepository extends JpaRepository<RiotAccount, Long> {
    // 추후 성능 상 문제 발생할 시: Projection 변경 고려 (조회용)
    Optional<RiotAccount> findByGameNameAndTagLine(String gameName, String tagLine);
    Optional<RiotAccount> findByPuuid(String puuid);
    List<RiotAccount> findAllByIdIn(List<Long> ids);
    List<RiotAccount> findAllByPuuidIn(List<String> puuids);
}
