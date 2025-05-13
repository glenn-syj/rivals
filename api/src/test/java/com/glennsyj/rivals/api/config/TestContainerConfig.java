package com.glennsyj.rivals.api.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MariaDBContainer;

import javax.sql.DataSource;

@TestConfiguration
public class TestContainerConfig {
    private static final MariaDBContainer<?> mariaDB;

    static {
        mariaDB = new MariaDBContainer<>("mariadb:latest")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true)  // 컨테이너 재사용 (테스트 실행 시간 단축)
                .withUrlParam("characterEncoding", "UTF-8")
                .withUrlParam("serverTimezone", "UTC+8")
                .withUrlParam("rewriteBatchedStatements", "true");

        mariaDB.start();  // 컨테이너 시작
    }

    @Bean
    public static DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(mariaDB.getJdbcUrl())
                .username(mariaDB.getUsername())
                .password(mariaDB.getPassword())
                .build();
    }
}
