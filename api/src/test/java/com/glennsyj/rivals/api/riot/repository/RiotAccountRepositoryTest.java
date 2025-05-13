package com.glennsyj.rivals.api.riot.repository;

import com.glennsyj.rivals.api.config.TestContainerConfig;
import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@Import(TestContainerConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RiotAccountRepositoryTest {

    @Autowired
    private RiotAccountRepository riotAccountRepository;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    void cleanup() {
        entityManager.clear();
    }

    List<RiotAccount> createRandomAccounts() {
        Random random = new Random();

        return IntStream.range(0, 10000)
                .mapToObj(i -> new RiotAccount(
                        // 랜덤 게임명 (5-10자)
                        random.ints(random.nextInt(5, 11), 97, 123)
                                .mapToObj(ch -> String.valueOf((char) ch))
                                .collect(Collectors.joining()),

                        // 랜덤 태그라인 (2-4자)
                        random.ints(random.nextInt(2, 5), 65, 91)
                                .mapToObj(ch -> String.valueOf((char) ch))
                                .collect(Collectors.joining()),

                        // 랜덤 puuid (78자)
                        random.ints(78, 48, 123)
                                .filter(j -> (j <= 57 || j >= 97))  // 숫자와 소문자만
                                .mapToObj(ch -> String.valueOf((char) ch))
                                .collect(Collectors.joining())
                ))
                .toList();
    }

    @Test
    void 동일_puuid로_중복_저장되지_않는다() {
        // given
        RiotAccount account1 = new RiotAccount("Hide", "KR1", "puuid123");
        RiotAccount account2 = new RiotAccount("Hide", "KR2", "puuid123");  // 같은 puuid

        // when
        riotAccountRepository.saveAndFlush(account1);

        // then
        assertThatThrownBy(() -> riotAccountRepository.saveAndFlush(account2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 대량의_데이터_저장시_성능이_일정하다() {
        // given
        List<RiotAccount> accounts = IntStream.range(0, 10000)
                .mapToObj(i -> new RiotAccount(
                        "Player" + i,
                        "KR" + i,
                        "puuid" + i
                ))
                .collect(Collectors.toList());

        // when
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        riotAccountRepository.saveAll(accounts);
        stopWatch.stop();

        // then
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(2000);  // 2초 이내 처리
    }

    @Test
    void 캐시_없이_인덱스를_활용한_조회가_빠르다() {
        // given
        List<RiotAccount> accounts = createRandomAccounts();
        riotAccountRepository.saveAll(accounts);
        entityManager.flush();
        entityManager.clear();

        // 웜업 쿼리
        riotAccountRepository.findByGameNameAndTagLine(accounts.get(0).getGameName(), accounts.get(0).getTagLine());

        // when
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Optional<RiotAccount> found = riotAccountRepository
                .findByGameNameAndTagLine(accounts.get(5000).getGameName(), accounts.get(5000).getTagLine());
        stopWatch.stop();

        // then
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(50);  // 50ms 이내 조회
        assertThat(found).isPresent();
    }
}
