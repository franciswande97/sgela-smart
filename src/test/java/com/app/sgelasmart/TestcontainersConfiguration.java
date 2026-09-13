package com.app.sgelasmart;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.boot.devtools.restart.RestartScope;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
  @RestartScope
	PostgreSQLContainer postgresContainer() {
    String dbName = System.getenv().getOrDefault("DEV_DB_NAME", "test");
    String dbUser = System.getenv().getOrDefault("DEV_DB_USER", "test");
    String dbPass = System.getenv().getOrDefault("DEV_DB_PASS", "test");
		return new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));
	}

}
