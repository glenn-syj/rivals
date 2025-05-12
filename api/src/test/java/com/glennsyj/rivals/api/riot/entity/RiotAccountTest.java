package com.glennsyj.rivals.api.riot.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
class RiotAccountTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void 동일_PUUID를_가진_엔티티는_동일하다고_판단한다() {
        // given
        String samePuuid = "same-puuid";
        RiotAccount account1 = new RiotAccount("hide", "KR1", samePuuid);
        RiotAccount account2 = new RiotAccount("hide", "KR1", samePuuid);

        // when & then
        assertThat(account1).isEqualTo(account2);
        assertThat(account1.hashCode()).isEqualTo(account2.hashCode());
    }

    @Test
    void 서로_다른_PUUID를_가진_엔티티는_다르다고_판단한다() {
        // given
        RiotAccount account1 = new RiotAccount("hide", "KR1", "puuid1");
        RiotAccount account2 = new RiotAccount("hide", "KR1", "puuid2");

        // when & then
        assertThat(account1).isNotEqualTo(account2);
        assertThat(account1.hashCode()).isNotEqualTo(account2.hashCode());
    }

    @Test
    void 프록시_객체와_실제_엔티티의_동등성을_보장한다() {
        // given
        RiotAccount account = new RiotAccount("hide", "KR1", "puuid123");
        entityManager.persist(account);
        entityManager.flush();
        entityManager.clear();

        // when
        RiotAccount proxy = entityManager.getReference(RiotAccount.class, account.getId());

        assertThat(Hibernate.isInitialized(proxy)).isFalse();
        proxy.getPuuid();
        assertThat(Hibernate.isInitialized(proxy)).isTrue();

        entityManager.clear();
        RiotAccount loaded = entityManager.find(RiotAccount.class, account.getId());

        // then
        assertThat(proxy).isInstanceOf(HibernateProxy.class);
        assertThat(loaded).isNotInstanceOf(HibernateProxy.class);
        assertThat(proxy).isEqualTo(loaded);
        assertThat(loaded).isEqualTo(proxy);
        assertThat(proxy.hashCode()).isEqualTo(loaded.hashCode());
    }

    @Test
    void Set에서_동일_엔티티_중복_추가를_방지한다() {
        // given
        String samePuuid = "same-puuid2";
        RiotAccount account1 = new RiotAccount("hide", "KR1", samePuuid);
        RiotAccount account2 = new RiotAccount("hide", "KR1", samePuuid);

        // when
        Set<RiotAccount> accounts = new HashSet<>();
        accounts.add(account1);
        accounts.add(account2);

        // then
        assertThat(accounts).hasSize(1);
    }

    @Test
    void 프록시_객체가_Set에서_정상_동작한다() {
        // given
        RiotAccount account = new RiotAccount("hide", "KR1", "puuid1234");
        entityManager.persist(account);
        entityManager.flush();
        entityManager.clear();

        RiotAccount proxy = entityManager.getReference(RiotAccount.class, account.getId());
        RiotAccount loaded = entityManager.find(RiotAccount.class, account.getId());

        // when
        Set<RiotAccount> accounts = new HashSet<>();
        accounts.add(proxy);
        accounts.add(loaded);

        // then
        assertThat(accounts).hasSize(1);
    }

    @Test
    void 다른_타입의_객체와_비교시_false를_반환한다() {
        // given
        RiotAccount account = new RiotAccount("hide", "KR1", "puuid12345");
        Object other = new Object();

        // when & then
        assertThat(account).isNotEqualTo(other);
    }

    @Test
    void null과_비교시_false를_반환한다() {
        // given
        RiotAccount account = new RiotAccount("hide", "KR1", "puuid123456");

        // when & then
        assertThat(account).isNotEqualTo(null);
    }
}
