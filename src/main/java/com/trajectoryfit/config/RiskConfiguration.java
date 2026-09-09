package com.trajectoryfit.config;

import com.trajectoryfit.risk.RiskService;
import com.trajectoryfit.risk.config.RiskConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the framework-free scoring core into the Spring context. Keeping the
 * {@link RiskService} and {@link RiskConfig} free of Spring annotations lets them be
 * compiled and validated with plain {@code javac} against the archetypes; the binding to
 * externalized {@code trajectoryfit.risk.*} properties happens here instead.
 */
@Configuration
public class RiskConfiguration {

    /**
     * The threshold configuration, bound from {@code trajectoryfit.risk.*} in
     * application.yml (or environment). Defaults reproduce the validated reference.
     */
    @Bean
    @ConfigurationProperties(prefix = "trajectoryfit.risk")
    public RiskConfig riskConfig() {
        return RiskConfig.defaults();
    }

    @Bean
    public RiskService riskService(RiskConfig riskConfig) {
        return new RiskService(riskConfig);
    }
}
