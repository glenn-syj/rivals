package com.glennsyj.rivals.api.riot.service;

import com.glennsyj.rivals.api.riot.RiotAccountClient;
import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.model.RiotAccountResponse;
import com.glennsyj.rivals.api.riot.repository.RiotAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiotAccountManager {
    private final RiotAccountRepository riotAccountRepository;
    private final RiotAccountClient riotAccountClient;

    public RiotAccountManager(RiotAccountRepository riotAccountRepository, RiotAccountClient riotAccountClient) {
        this.riotAccountRepository = riotAccountRepository;
        this.riotAccountClient = riotAccountClient;
    }

    @Transactional
    public RiotAccount findOrRegisterAccount(String gameName, String tagLine) {
        return riotAccountRepository
                .findByGameNameAndTagLine(gameName, tagLine)
                .orElseGet(() -> registerNewAccount(gameName, tagLine));
    }

    @Transactional
    public RiotAccount renewAccount(String gameName, String tagLine) {
        RiotAccount fetchedAccount = riotAccountRepository
                .findByGameNameAndTagLine(gameName, tagLine)
                .orElseThrow(() -> new IllegalStateException("Account not found"));

        fetchedAccount.renewUpdatedAt();
        return fetchedAccount;
    }

    private RiotAccount registerNewAccount(String gameName, String tagLine) {
        RiotAccountResponse response = riotAccountClient.getAccountInfo(gameName, tagLine);
        // RiotAccount::updatedAt은 생성 시 자동으로 초기화
        RiotAccount account = new RiotAccount(
                response.gameName(),
                response.tagLine(),
                response.puuid()
        );
        return riotAccountRepository.save(account);
    }
}
