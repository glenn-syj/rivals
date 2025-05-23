package com.glennsyj.rivals.api.rivalry.service;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.repository.RiotAccountRepository;
import com.glennsyj.rivals.api.rivalry.entity.Rivalry;
import com.glennsyj.rivals.api.rivalry.entity.RivalryParticipant;
import com.glennsyj.rivals.api.rivalry.model.RivalryCreationDto;
import com.glennsyj.rivals.api.rivalry.model.RivalryParticipantDto;
import com.glennsyj.rivals.api.rivalry.repository.RivalryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
public class RivalryService {

    private RivalryRepository rivalryRepository;
    private RiotAccountRepository riotAccountRepository;

    public RivalryService(RivalryRepository rivalryRepository, RiotAccountRepository riotAccountRepository) {
        this.rivalryRepository = rivalryRepository;
        this.riotAccountRepository = riotAccountRepository;
    }

    @Transactional
    public Long createRivalryFrom(RivalryCreationDto creationDto) {

        List<Long> accountIds = creationDto.participants().stream()
                .map((RivalryParticipantDto::id))
                .toList();

        List<RiotAccount> accounts = riotAccountRepository.findAllByIdIn(accountIds);

        // 중간 검증: account를 하나도 찾을 수 없을 시
        if (accounts.isEmpty()) {
            throw new IllegalArgumentException("Some of accounts not found");
        }

        HashMap<Long, RiotAccount> accountMap = new HashMap<>();

        for (RiotAccount account : accounts) {
            accountMap.put(account.getId(), account);
        }

        Rivalry rivalry = new Rivalry();

        for (RivalryParticipantDto dto : creationDto.participants()) {
            RivalryParticipant participant = new RivalryParticipant(
                    accountMap.get(dto.id()), rivalry, dto.side()
            );
            rivalry.addParticipant(participant);
        }

        rivalry = rivalryRepository.save(rivalry);

        // 최종 검증: dto 내 participant 수와 영속화된 participant 수 비교
        if (accountIds.size() != rivalry.getParticipants().size()) {
            throw new IllegalArgumentException("Some of accounts not found");
        }

        return rivalry.getId();
    }
}
