package com.glennsyj.rivals.api.riot.service;

import com.glennsyj.rivals.api.config.TestContainerConfig;
import com.glennsyj.rivals.api.riot.RiotApiClient;
import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.model.RiotAccountResponse;
import com.glennsyj.rivals.api.riot.repository.RiotAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@Import(TestContainerConfig.class)
class RiotAccountManagerIntegrationTest {
    @Autowired
    private RiotAccountManager accountManager;

    @Autowired
    private RiotAccountRepository accountRepository;

    @MockitoBean
    private RiotApiClient riotApiClient;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
    }

    @Test
    void 정상_계정_등록_및_조회_흐름() {
        // given
        when(riotApiClient.getAccountInfo("Hide", "KR1"))
                .thenReturn(new RiotAccountResponse("puuid123","Hide", "KR1" ));

        // when
        accountManager.findOrRegisterAccount("Hide", "KR1");
        System.out.println(accountRepository.count());
        // then
        // 1. DB 저장 확인 (실제 MariaDB에 저장)
        assertThat(accountRepository.findByPuuid("puuid123"))
                .isPresent()
                .get()
                .satisfies(account -> {
                    assertThat(account.getGameName()).isEqualTo("Hide");
                    assertThat(account.getTagLine()).isEqualTo("KR1");
                });

        // 2. 이미 등록되었으므로 ApiClient는 더 호출되어선 안됨
        accountManager.findOrRegisterAccount("Hide", "KR1");
        verify(riotApiClient, times(1)).getAccountInfo(any(), any());
    }

    @Test
    void DB_제약조건_검증() {
        // given
        when(riotApiClient.getAccountInfo(any(), any()))
                .thenReturn(new RiotAccountResponse("puuid123", "Hide", "KR1"));

        // when
        accountManager.findOrRegisterAccount("Hide", "KR1");

        // then - unique constraint 검증
        assertThatThrownBy(() ->
                accountRepository.save(new RiotAccount("Hide2", "KR2", "puuid123"))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
