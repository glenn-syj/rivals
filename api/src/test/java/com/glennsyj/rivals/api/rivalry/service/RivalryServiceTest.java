package com.glennsyj.rivals.api.rivalry.service;

import com.glennsyj.rivals.api.config.EntityTestUtil;
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
import com.glennsyj.rivals.api.tft.repository.TftLeagueEntryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RivalryServiceTest {

    @Mock
    private RivalryRepository rivalryRepository;

    @Mock
    private RiotAccountRepository riotAccountRepository;

    @Mock
    private TftLeagueEntryRepository tftLeagueEntryRepository;

    @InjectMocks
    private RivalryService rivalryService;

    @Test
    @DisplayName("정상적인 DTO로 라이벌리 생성 시 성공하고 ID를 반환한다")
    void createRivalrySuccess() {
        // given
        RiotAccount account1 = new RiotAccount("user1", "tag1", "puuid1");
        RiotAccount account2 = new RiotAccount("user2", "tag2", "puuid2");
        EntityTestUtil.setId(account1, 1L);
        EntityTestUtil.setId(account2, 2L);

        List<RivalryParticipantDto> participants = List.of(
                new RivalryParticipantDto(String.valueOf(account1.getId()), RivalSide.LEFT),
                new RivalryParticipantDto(String.valueOf(account2.getId()), RivalSide.RIGHT)
        );
        RivalryCreationDto creationDto = new RivalryCreationDto(participants);

        // Mock repository responses
        when(riotAccountRepository.findAllByIdIn(List.of(1L, 2L)))
                .thenReturn(List.of(account1, account2));
        when(rivalryRepository.save(any())).thenAnswer(invocation -> {
            Rivalry rivalry = invocation.getArgument(0);
            EntityTestUtil.setId(rivalry,1L);
            return rivalry;
        });

        // when
        Long rivalryId = rivalryService.createRivalryFrom(creationDto);

        // then
        assertThat(rivalryId).isEqualTo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 계정 ID로 라이벌리 생성 시 예외가 발생한다")
    void createRivalryWithNonExistentAccount() {
        // given
        List<RivalryParticipantDto> participants = List.of(
                new RivalryParticipantDto(String.valueOf(999L), RivalSide.LEFT),
                new RivalryParticipantDto(String.valueOf(888L), RivalSide.RIGHT)
        );
        RivalryCreationDto creationDto = new RivalryCreationDto(participants);

        // Mock repository response for non-existent accounts
        when(riotAccountRepository.findAllByIdIn(List.of(999L, 888L)))
                .thenReturn(List.of());

        // when & then
        assertThatThrownBy(() -> rivalryService.createRivalryFrom(creationDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Some of accounts not found");
    }

    @Test
    @DisplayName("같은 진영에 중복된 계정으로 라이벌리 생성 시 예외가 발생한다")
    void createRivalryWithDuplicateAccountOnSameSide() {
        // given
        RiotAccount account = new RiotAccount("user1", "tag1", "puuid1");
        EntityTestUtil.setId(account, 1L);

        List<RivalryParticipantDto> participants = List.of(
                new RivalryParticipantDto(String.valueOf(account.getId()), RivalSide.LEFT),
                new RivalryParticipantDto(String.valueOf(account.getId()), RivalSide.LEFT),
                new RivalryParticipantDto(String.valueOf(account.getId()), RivalSide.RIGHT)
        );
        RivalryCreationDto creationDto = new RivalryCreationDto(participants);

        // Mock repository response
        when(riotAccountRepository.findAllByIdIn(List.of(1L, 1L, 1L)))
                .thenReturn(List.of(account));

        // when & then
        assertThatThrownBy(() -> rivalryService.createRivalryFrom(creationDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Participant already exists on the same side.");
    }

    @Test
    @DisplayName("라이벌리 ID로 상세 정보를 조회한다")
    void findRivalryFrom_Success() {
        // given
        // 1. 라이벌리와 참가자 설정
        RiotAccount account1 = new RiotAccount("user1", "tag1", "puuid1");
        RiotAccount account2 = new RiotAccount("user2", "tag2", "puuid2");
        EntityTestUtil.setId(account1, 1L);
        EntityTestUtil.setId(account2, 2L);

        Rivalry rivalry = new Rivalry();
        EntityTestUtil.setId(rivalry, 1L);

        RivalryParticipant participant1 = new RivalryParticipant(account1, rivalry, RivalSide.LEFT);
        RivalryParticipant participant2 = new RivalryParticipant(account2, rivalry, RivalSide.RIGHT);
        rivalry.addParticipant(participant1);
        rivalry.addParticipant(participant2);

        // 2. TFT 상태 정보 설정
        TftLeagueEntry entry1 = createMockTftEntry(account1, "DIAMOND", "I");
        TftLeagueEntry entry2 = createMockTftEntry(account2, "PLATINUM", "II");

        // 3. Mock 설정
        when(rivalryRepository.findById(1L)).thenReturn(Optional.of(rivalry));
        when(riotAccountRepository.findAllById(anyList())).thenReturn(List.of(account1, account2));
        when(tftLeagueEntryRepository.findAllById(anyList())).thenReturn(List.of(entry1, entry2));

        // when
        RivalryDetailDto result = rivalryService.findRivalryFrom(1L);

        // then
        assertThat(result.rivalryId()).isEqualTo(1L);

        // LEFT 팀 검증
        assertThat(result.leftStats().size()).isEqualTo(1);
        ParticipantStatDto leftParticipant = result.leftStats().get(0);
        assertThat(leftParticipant.fullName()).isEqualTo("user1#tag1");
        assertThat(leftParticipant.statistics().tier()).isEqualTo("DIAMOND");

        // RIGHT 팀 검증
        assertThat(result.rightStats().size()).isEqualTo(1);
        ParticipantStatDto rightParticipant = result.rightStats().get(0);
        assertThat(rightParticipant.fullName()).isEqualTo("user2#tag2");
        assertThat(rightParticipant.statistics().tier()).isEqualTo("PLATINUM");
    }

    @Test
    @DisplayName("존재하지 않는 라이벌리 ID로 조회시 예외가 발생한다")
    void findRivalryFrom_NotFound() {
        // given
        when(rivalryRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> rivalryService.findRivalryFrom(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // 테스트용 TftLeagueEntry 생성 헬퍼 메서드
    private TftLeagueEntry createMockTftEntry(RiotAccount account, String tier, String rank) {
        TftLeagueEntryResponse response = new TftLeagueEntryResponse(
                account.getPuuid(),
                "league-id",
                "summoner-id",
                "RANKED_TFT",
                tier,
                rank,
                100,
                10,
                5,
                false,
                false,
                false,
                false,
                null
        );
        return new TftLeagueEntry(account, response);
    }
}