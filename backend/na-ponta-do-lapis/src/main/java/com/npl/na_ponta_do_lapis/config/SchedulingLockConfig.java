package com.npl.na_ponta_do_lapis.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

/**
 * Configura lock distribuído via banco de dados para evitar que tarefas @Scheduled
 * rodem simultaneamente em múltiplas réplicas do backend.
 */
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT30M")
public class SchedulingLockConfig {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS shedlock (
                    name VARCHAR(64) NOT NULL,
                    lock_until TIMESTAMP(3) NOT NULL,
                    locked_at TIMESTAMP(3) NOT NULL,
                    locked_by VARCHAR(255) NOT NULL,
                    PRIMARY KEY (name)
                )
                """);

        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(jdbcTemplate)
                        .usingDbTime()
                        .build()
        );
    }
}

