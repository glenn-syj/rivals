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
        rivalry.getParticipants().add(participant);
        Rivalry found = rivalryRepository.save(rivalry);

        // then
        assertThat(found.getParticipants().size()).isEqualTo(1);
    }

}
