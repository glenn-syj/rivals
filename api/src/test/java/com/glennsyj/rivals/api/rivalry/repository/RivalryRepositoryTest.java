package com.glennsyj.rivals.api.rivalry.repository;

import com.glennsyj.rivals.api.config.TestContainerConfig;
import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.rivalry.entity.RivalSide;
import com.glennsyj.rivals.api.rivalry.entity.Rivalry;
import com.glennsyj.rivals.api.rivalry.entity.RivalryParticipant;
import com.glennsyj.rivals.api.rivalry.repository.RivalryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfig.class)
@Testcontainers
public class RivalryRepositoryTest {

    @Autowired
    private RivalryRepository rivalryRepository;

    /*
        Rivalry 엔티티 행위 및
        RivalryParticipantRepository 필요 여부 확인 위한 테스트
     */
    @Test
    void 라이벌리를_생성하고_참여자를_추가하면_참여자도_영속화한다() {

        // given
        Rivalry rivalry = new Rivalry();
        RiotAccount account = new RiotAccount("Hide", "KR1", "puuid0");
        RivalryParticipant participant = new RivalryParticipant(account, rivalry, RivalSide.LEFT);

        // when
        rivalry.addParticipant(participant);
        Rivalry found = rivalryRepository.save(rivalry);

        // then
        assertThat(found.getParticipants().size()).isEqualTo(1);
    }

    @Test
    void 양측에_같은_계정이_하나_포함되어도_된다() {

        // given
        Rivalry rivalry = new Rivalry();
        RiotAccount accountBothSide = new RiotAccount("Hide", "KR1", "puuid0");
        RiotAccount accountLeftSide = new RiotAccount("Hide", "KR2", "puuid1");
        RiotAccount accountRightSide = new RiotAccount("Hide", "KR3", "puuid2");
        RivalryParticipant participant = new RivalryParticipant(accountBothSide, rivalry, RivalSide.LEFT);
        RivalryParticipant participant2 = new RivalryParticipant(accountLeftSide, rivalry, RivalSide.RIGHT);
        RivalryParticipant participant3 = new RivalryParticipant(accountRightSide, rivalry, RivalSide.LEFT);
        RivalryParticipant participant4 = new RivalryParticipant(accountBothSide, rivalry, RivalSide.RIGHT);

        // when
        rivalry.addParticipant(participant);
        rivalry.addParticipant(participant2);
        rivalry.addParticipant(participant3);
        rivalry.addParticipant(participant4);
        Rivalry found = rivalryRepository.save(rivalry);

        // then
        assertThat(found.getParticipants().size()).isEqualTo(4);
    }

    @Test
    void 한쪽에_같은_계정이_중복_참여되면_오류가_난다() {

        // given
        Rivalry rivalry = new Rivalry();
        RiotAccount account = new RiotAccount("Hide", "KR1", "puuid0");
        RivalryParticipant participant = new RivalryParticipant(account, rivalry, RivalSide.LEFT);
        RivalryParticipant participant2 = new RivalryParticipant(account, rivalry, RivalSide.LEFT);

        // when * then
        rivalry.addParticipant(participant);

        assertThatThrownBy(() -> rivalry.addParticipant(participant2))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
