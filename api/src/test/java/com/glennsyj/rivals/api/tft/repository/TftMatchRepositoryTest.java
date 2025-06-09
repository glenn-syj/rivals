package com.glennsyj.rivals.api.tft.repository;

import com.glennsyj.rivals.api.config.TestContainerConfig;
import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.entity.match.TftMatchParticipant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfig.class)
@Testcontainers
class TftMatchRepositoryTest {

    @Autowired
    private TftMatchRepository tftMatchRepository;

    @Test
    @DisplayName("특정 puuid를 가진 참가자의 최근 20개 매치를 조회한다")
    void findTop20ByParticipantsPuuidOrderByGameCreationDesc() {
        // given
        String testPuuid = "test-puuid";

        // 25개의 테스트 매치 생성 (gameCreation 시간을 다르게 설정)
        for (int i = 0; i < 25; i++) {
            TftMatch match = createTestMatch(
                    "match-" + i
            );

            // 매치에 참가자 추가
            TftMatchParticipant participant = createTestParticipant(testPuuid);
            match.addParticipant(participant);

            tftMatchRepository.save(match);
        }

        // when
        List<TftMatch> matches = tftMatchRepository.findTop20ByParticipantsPuuidOrderByGameCreationDesc(testPuuid);

        // then
        assertThat(matches).isNotNull();
        assertThat(matches).hasSize(20); // 정확히 20개의 매치를 반환하는지 확인

        // gameCreation 기준 내림차순 정렬 확인
        assertThat(matches)
                .isSortedAccordingTo((m1, m2) -> m2.getGameCreation().compareTo(m1.getGameCreation()));

        // 모든 매치에 testPuuid를 가진 참가자가 있는지 확인
        assertThat(matches)
                .allMatch(match -> match.getParticipants().stream()
                        .anyMatch(participant -> participant.getPuuid().equals(testPuuid)));
    }

    private TftMatch createTestMatch(String matchId) {
        return new TftMatch(
                matchId, 
                "1.0",
                System.currentTimeMillis(), // 현재 시간으로 gameCreation 설정
                1L, // gameId
                System.currentTimeMillis(), // gameDateTime
                10.0, // gameLength
                "version", 
                "variation", 
                1, 
                1, 
                "type", 
                "core", 
                1, 
                "result"
        );
    }

    private TftMatchParticipant createTestParticipant(String puuid) {
        return new TftMatchParticipant(
                puuid,
                100,  // goldLeft
                8,    // lastRound
                1000, // missionsPlayerScore2
                8,    // level
                4,    // placement
                3,    // playersEliminated
                "TestPlayer", // riotIdGameName
                "TEST",      // riotIdTagline
                20.0,        // timeEliminated
                1000,        // totalDamageToPlayers
                false,       // win
                null,        // companion
                List.of(),   // traits
                List.of()    // units
        );
    }
}
