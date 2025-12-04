package org.policedog.registry.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Component;

@Component
@EnableJpaAuditing
@ConditionalOnProperty(name = "spring.jpa.auditing.enabled", havingValue = "true", matchIfMissing = true)
public class JpaConfig {
}
