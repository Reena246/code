package com.accesscontrol.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration
 * Access UI at: http://localhost:8080/swagger-ui.html
 * Access API docs at: http://localhost:8080/api-docs
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI accessControlOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Access Control System API")
                        .description(
                                "Production-grade Physical Access Control System\n\n" +
                                "**SECURITY NOTICE:**\n" +
                                "- All endpoints accept/return encrypted payloads (AES-256/CBC/PKCS5Padding)\n" +
                                "- Random IV per request via X-IV header (Base64 encoded)\n" +
                                "- Same IV used for request decryption and response encryption\n" +
                                "- Swagger shows unencrypted payloads for internal testing only\n\n" +
                                "**FEATURES:**\n" +
                                "- Real-time card validation\n" +
                                "- Door event logging (OPEN/CLOSE/FORCED)\n" +
                                "- Offline/online controller support\n" +
                                "- Database synchronization with minimal payloads\n" +
                                "- Bulk event upload after reconnect\n" +
                                "- Chronological audit logging\n\n" +
                                "**DATABASE:** access_control_db (MySQL)\n" +
                                "**FRAMEWORK:** Spring Boot 3.4.1 with Java 21\n" +
                                "**LOGGING:** Log4j2 with structured logging"
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Access Control System Team")
                                .email("support@accesscontrol.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://accesscontrol.com/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.accesscontrol.com")
                                .description("Production Server")
                ));
    }
}
