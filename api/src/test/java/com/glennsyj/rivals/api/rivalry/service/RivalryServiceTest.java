package com.glennsyj.rivals.api.rivalry.service;

import com.glennsyj.rivals.api.config.EntityTestUtil;
import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.repository.RiotAccountRepository;
import com.glennsyj.rivals.api.rivalry.entity.RivalSide;
import com.glennsyj.rivals.api.rivalry.entity.Rivalry;
import com.glennsyj.rivals.api.rivalry.model.RivalryCreationDto;
import com.glennsyj.rivals.api.rivalry.model.RivalryParticipantDto;
import com.glennsyj.rivals.api.rivalry.repository.RivalryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RivalryServiceTest {

    @Mock
    private RivalryRepository rivalryRepository;

    @Mock
    private RiotAccountRepository riotAccountRepository;

    private RivalryService rivalryService;

    @BeforeEach
    void setUp() {
        rivalryService = new RivalryService(rivalryRepository, riotAccountRepository);
    }

    @Test
    @DisplayName("정상적인 DTO로 라이벌리 생성 시 성공하고 ID를 반환한다")
    void createRivalrySuccess() {
        // given
        RiotAccount account1 = new RiotAccount("user1", "tag1", "puuid1");
        RiotAccount account2 = new RiotAccount("user2", "tag2", "puuid2");
        EntityTestUtil.setId(account1, 1L);
        EntityTestUtil.setId(account2, 2L);

        List<RivalryParticipantDto> participants = List.of(
                new RivalryParticipantDto(account1.getId(), RivalSide.LEFT),
                new RivalryParticipantDto(account2.getId(), RivalSide.RIGHT)
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
                new RivalryParticipantDto(999L, RivalSide.LEFT),
                new RivalryParticipantDto(888L, RivalSide.RIGHT)
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
                new RivalryParticipantDto(account.getId(), RivalSide.LEFT),
                new RivalryParticipantDto(account.getId(), RivalSide.LEFT),
                new RivalryParticipantDto(account.getId(), RivalSide.RIGHT)
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
}