package com.glennsyj.rivals.api.riot.service;

import com.glennsyj.rivals.api.riot.RiotAccountClient;
import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.model.RiotAccountResponse;
import com.glennsyj.rivals.api.riot.repository.RiotAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)  // 단위 테스트
class RiotAccountManagerTest {

    @Mock
    private RiotAccountRepository repository;

    @Mock
    private RiotAccountClient apiClient;

    @InjectMocks
    private RiotAccountManager manager;

    @Test
    void API_호출_실패시_예외_전파() {
        // given
        when(repository.findByGameNameAndTagLine(any(), any()))
                .thenReturn(Optional.empty());
        when(apiClient.getAccountInfo(any(), any()))
                .thenThrow(new IllegalStateException("API Error"));

        // when & then
        assertThatThrownBy(() ->
                manager.findOrRegisterAccount("Hide", "KR1")
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void DB에_계정이_없으면_API_호출하고_저장() {
        // given
        when(repository.findByGameNameAndTagLine("Hide", "KR1"))
                .thenReturn(Optional.empty());
        when(apiClient.getAccountInfo("Hide", "KR1"))
                .thenReturn(new RiotAccountResponse("Hide", "KR1", "puuid123"));

        // when
        manager.findOrRegisterAccount("Hide", "KR1");

        // then
        verify(repository).save(any(RiotAccount.class));
    }

    @Test
    void 계정_갱신시_업데이트_시간_변경() throws InterruptedException {
        // given
        RiotAccount account = new RiotAccount("Hide", "KR1", "puuid123");
        LocalDateTime beforeUpdate = account.getUpdatedAt();
        when(repository.findByGameNameAndTagLine("Hide", "KR1"))
                .thenReturn(Optional.of(account));

        // when
        Thread.sleep(500);
        manager.renewAccount("Hide", "KR1");

        // then
        assertThat(account.getUpdatedAt())
                .isAfter(beforeUpdate);
    }
}
