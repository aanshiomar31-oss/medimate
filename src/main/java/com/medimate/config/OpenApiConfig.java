package com.medimate.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mediMateOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MediMate — Enterprise Medicine Supply Chain & Custody Engine")
                        .description("High-reliability backend service for pharmaceutical supply chains, batch-level custody transfers, " +
                                "cold-chain transit SLA monitoring, anti-counterfeit verification, and process mining event audit trails.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Aanshi Omar")
                                .email("aanshiomar31@gmail.com")
                                .url("https://github.com/aanshiomar31-oss/medimate"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
