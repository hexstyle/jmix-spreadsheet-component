package com.hexstyle.jmixspreadsheet.test_support;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.util.Map;

/**
 * Boots an embedded PostgreSQL instance for tests and overrides Jmix datasource properties.
 */
public class EmbeddedPostgresEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String ENABLED_PROPERTY = "test.embedded-postgres.enabled";
    private static final String ENABLED_DEFAULT = "true";
    private static final EmbeddedPostgresHolder HOLDER = new EmbeddedPostgresHolder();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!Boolean.parseBoolean(environment.getProperty(ENABLED_PROPERTY, ENABLED_DEFAULT))) {
            return;
        }

        EmbeddedPostgres postgres = HOLDER.getOrStart();
        String jdbcUrl = postgres.getJdbcUrl("postgres", "postgres");

        Map<String, Object> overrides = Map.of(
                "main.datasource.url", jdbcUrl,
                "main.datasource.username", "postgres",
                "main.datasource.password", "postgres",
                "main.datasource.driver-class-name", "org.postgresql.Driver"
        );
        environment.getPropertySources().addFirst(new MapPropertySource("embeddedPostgresTestDatasource", overrides));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private static final class EmbeddedPostgresHolder {
        private EmbeddedPostgres embeddedPostgres;

        private synchronized EmbeddedPostgres getOrStart() {
            if (embeddedPostgres != null) {
                return embeddedPostgres;
            }
            try {
                embeddedPostgres = EmbeddedPostgres.builder().setPort(0).start();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        embeddedPostgres.close();
                    } catch (IOException ignored) {
                        // Best-effort shutdown.
                    }
                }, "embedded-postgres-shutdown"));
                return embeddedPostgres;
            } catch (IOException e) {
                throw new IllegalStateException("Failed to start embedded PostgreSQL for tests", e);
            }
        }
    }
}
