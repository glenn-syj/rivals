package com.glennsyj.rivals.api.rivalry.service;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.repository.RiotAccountRepository;
import com.glennsyj.rivals.api.rivalry.entity.RivalSide;
import com.glennsyj.rivals.api.rivalry.entity.Rivalry;
import com.glennsyj.rivals.api.rivalry.entity.RivalryParticipant;
import com.glennsyj.rivals.api.rivalry.model.ParticipantStatDto;
import com.glennsyj.rivals.api.rivalry.model.RivalryCreationDto;
import com.glennsyj.rivals.api.rivalry.model.RivalryDetailDto;
import com.glennsyj.rivals.api.rivalry.model.RivalryParticipantDto;
import com.glennsyj.rivals.api.rivalry.repository.RivalryRepository;
import com.glennsyj.rivals.api.tft.entity.TftLeagueEntry;
import com.glennsyj.rivals.api.tft.model.TftLeagueEntryResponse;
import com.glennsyj.rivals.api.tft.model.TftStatusDto;
import com.glennsyj.rivals.api.tft.repository.TftLeagueEntryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.annotations.NotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RivalryService {

    private RivalryRepository rivalryRepository;
    private RiotAccountRepository riotAccountRepository;
    private TftLeagueEntryRepository tftLeagueEntryRepository;

    public RivalryService(RivalryRepository rivalryRepository,
                          RiotAccountRepository riotAccountRepository,
                          TftLeagueEntryRepository tftLeagueEntryRepository) {
        this.rivalryRepository = rivalryRepository;
        this.riotAccountRepository = riotAccountRepository;
        this.tftLeagueEntryRepository = tftLeagueEntryRepository;
    }

    @Transactional
    public Long createRivalryFrom(RivalryCreationDto creationDto) {

        System.out.println("creationDto: " + creationDto);
        List<Long> accountIds = creationDto.participants().stream()
                .map((dto) -> Long.valueOf(dto.id()))
                .toList();
        System.out.println(accountIds);
        List<RiotAccount> accounts = riotAccountRepository.findAllById(accountIds);

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
                    accountMap.get(Long.valueOf(dto.id())), rivalry, dto.side()
            );
            rivalry.addParticipant(participant);
        }

        rivalry = rivalryRepository.save(rivalry);
        System.out.println("rv size: " + rivalry.getParticipants().size());
        // 최종 검증: dto 내 participant 수와 영속화된 participant 수 비교
        if (accountIds.size() != rivalry.getParticipants().size()) {
            throw new IllegalArgumentException("Some of accounts not found");
        }

        return rivalry.getId();
    }

    @Transactional(readOnly = true)
    public RivalryDetailDto findRivalryFrom(Long id) {

        // 1. Rivalry 정보 불러오고 캐시로 이용할 맵 초기화
        Rivalry rivalry = rivalryRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        Map<Long, String> nameCacheMap = new HashMap<>();
        Map<Long, TftStatusDto> leagueEntryCacheMap = new HashMap<>();

        // 2. participants 기반 정보 불러오기
        List<RivalryParticipant> participants = rivalry.getParticipants();
        List<Long> participantsAccountIds = participants.stream().map((participant) -> participant.getRiotAccount().getId()).toList();

        List<RiotAccount> accounts = riotAccountRepository.findAllById(participantsAccountIds);
        accounts.forEach(account -> nameCacheMap.put(account.getId(), account.getFullGameName()));

        List<TftLeagueEntry> leagueEntries = tftLeagueEntryRepository.findAllByAccount_IdIn(participantsAccountIds);
        leagueEntries.forEach(entry -> leagueEntryCacheMap.put(entry.getAccount().getId(), TftStatusDto.from(entry)));

        // 3. RivalryDetailDto 생성 및 반환
        List<ParticipantStatDto> leftStats = new ArrayList<>();
        List<ParticipantStatDto> rightStats = new ArrayList<>();

        for (RivalryParticipant participant : participants) {
            Long participantId = participant.getId();
            String fullName = nameCacheMap.get(participant.getRiotAccount().getId());
            TftStatusDto status = leagueEntryCacheMap.get(participant.getRiotAccount().getId());
            ParticipantStatDto participantStat = new ParticipantStatDto(participantId.toString(), fullName, status);

            if (participant.getSide() == RivalSide.LEFT) {
                leftStats.add(participantStat);
            } else {
                rightStats.add(participantStat);
            }
        }

        return new RivalryDetailDto(id.toString(), leftStats, rightStats, rivalry.getCreatedAt());
    }
}
