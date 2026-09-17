package com.zyd.ecmall;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.StandardEnvironment;
import static org.junit.jupiter.api.Assertions.*;

class DeploymentConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withInitializer(context -> {
                // 実際のPC・CIの環境変数を使わず、設定ファイルだけを検証する。
                context.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
                context.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
            })
            .withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    void defaultProfileIsProductionAndMissingHostDoesNotFallBackToLocalhost() {
        runner.run(context -> {
            var env = context.getEnvironment();
            assertArrayEquals(new String[]{"prod"}, env.getDefaultProfiles());
            assertThrows(IllegalArgumentException.class, () -> env.getRequiredProperty("spring.datasource.url"));
            assertThrows(IllegalArgumentException.class, () -> env.getRequiredProperty("spring.datasource.password"));
            assertThrows(IllegalArgumentException.class, () -> env.getRequiredProperty("jwt.secret"));
        });
    }

    @Test
    void productionUsesExistingRdsEnvironmentVariableNames() {
        runner.withPropertyValues(
                "RDS_HOSTNAME=database.example.invalid", "RDS_DB_NAME=ec_mall",
                "RDS_USERNAME=production-user", "RDS_PASSWORD=test-password",
                "JWT_SECRET=test-signing-key"
        ).run(context -> {
            var env = context.getEnvironment();
            assertTrue(env.getRequiredProperty("spring.datasource.url")
                    .startsWith("jdbc:mysql://database.example.invalid:3306/ec_mall?"));
            assertEquals("production-user", env.getProperty("spring.datasource.username"));
            assertEquals("test-password", env.getProperty("spring.datasource.password"));
            assertEquals("test-signing-key", env.getProperty("jwt.secret"));
            assertEquals("5000", env.getProperty("server.port"));
        });
    }

    @Test
    void localProfileUsesSeparateVariablesAndDevProxyPort() {
        runner.withPropertyValues(
                "spring.profiles.active=local", "LOCAL_DB_PASSWORD=local-password",
                "LOCAL_JWT_SECRET=local-key", "RDS_HOSTNAME=database.example.invalid",
                "RDS_USERNAME=production-user", "RDS_PASSWORD=production-password",
                "JWT_SECRET=production-key"
        ).run(context -> {
            var env = context.getEnvironment();
            assertTrue(env.getRequiredProperty("spring.datasource.url").startsWith("jdbc:mysql://192.168.56.11:3306/ec_mall?"));
            assertEquals("root", env.getProperty("spring.datasource.username"));
            assertEquals("local-password", env.getProperty("spring.datasource.password"));
            assertEquals("local-key", env.getProperty("jwt.secret"));
            assertEquals("8888", env.getProperty("server.port"));
        });
    }

    @Test
    void localSecretsHaveNoCommittedDefaults() {
        runner.withPropertyValues("spring.profiles.active=local").run(context -> {
            var env = context.getEnvironment();
            assertThrows(IllegalArgumentException.class, () -> env.getRequiredProperty("spring.datasource.password"));
            assertThrows(IllegalArgumentException.class, () -> env.getRequiredProperty("jwt.secret"));
        });
    }
}
