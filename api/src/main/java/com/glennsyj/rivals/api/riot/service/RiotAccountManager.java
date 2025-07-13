package com.glennsyj.rivals.api.riot.service;

import com.glennsyj.rivals.api.common.exception.RiotAccountNotFoundException;
import com.glennsyj.rivals.api.riot.RiotAccountClient;
import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.model.RiotAccountResponse;
import com.glennsyj.rivals.api.riot.repository.RiotAccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RiotAccountManager {
    private final RiotAccountRepository riotAccountRepository;
    private final RiotAccountClient riotAccountClient;

    public RiotAccountManager(RiotAccountRepository riotAccountRepository, RiotAccountClient riotAccountClient) {
        this.riotAccountRepository = riotAccountRepository;
        this.riotAccountClient = riotAccountClient;
    }

    @Transactional(readOnly = true)
    public Optional<RiotAccount> findAccount(String gameName, String tagLine) {
        return riotAccountRepository
                .findByGameNameAndTagLine(gameName, tagLine);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public RiotAccount registerNewAccountFromRiot(RiotAccountResponse response) {
        try {
            RiotAccount newAccount = new RiotAccount(response.gameName(), response.tagLine(), response.puuid());
            return riotAccountRepository.save(newAccount);
        } catch (DataIntegrityViolationException e) {
            return riotAccountRepository.findByGameNameAndTagLine(
                    response.gameName(),
                    response.tagLine()
            ).get(); // DataIntegrityViolationException 이 뜬다면 계정이 존재함
        }
    }

    @Transactional(readOnly = true)
    public Optional<RiotAccount> findByGameNameAndTagLine(String gameName, String tagLine) {
        return riotAccountRepository.findByGameNameAndTagLine(gameName.trim(), tagLine.trim());
    }

    @Transactional(readOnly = true)
    public Optional<RiotAccount> findByAccountId(Long accountId) {
        return riotAccountRepository.findById(accountId);
    }

    @Transactional
    public RiotAccount renewAccount(String gameName, String tagLine) {
        RiotAccount fetchedAccount = riotAccountRepository
                .findByGameNameAndTagLine(gameName, tagLine)
                .orElseThrow(() -> new RiotAccountNotFoundException(
                        "GameName: " + gameName + ", TagLine: " + tagLine + " 에 해당하는 Riot 계정을 찾을 수 없습니다."));

        fetchedAccount.renewUpdatedAt();
        return fetchedAccount;
    }

    public RiotAccountResponse fetchRiotAccountResponseFromRiot(String gameName, String tagLine) {
        return riotAccountClient.getAccountInfo(gameName, tagLine);
    }

    private RiotAccount registerNewAccount(String gameName, String tagLine) {
        RiotAccountResponse response = riotAccountClient.getAccountInfo(gameName.trim(), tagLine.trim());
        // RiotAccount::updatedAt은 생성 시 자동으로 초기화
        RiotAccount account = new RiotAccount(
                response.gameName().trim(),
                response.tagLine().trim(),
                response.puuid()
        );
        return riotAccountRepository.save(account);
    }
}
