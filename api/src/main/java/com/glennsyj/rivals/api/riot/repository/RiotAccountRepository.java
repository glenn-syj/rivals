package com.glennsyj.rivals.api.riot.repository;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiotAccountRepository extends JpaRepository<RiotAccount, Long> {
    Optional<RiotAccount> findByGameNameAndTagLine(String gameName, String tagLine);
    Optional<RiotAccount> findByPuuid(String puuid);
}
